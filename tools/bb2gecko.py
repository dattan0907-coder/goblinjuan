import json, sys, re

def num(v):
    v = round(float(v), 5)
    return int(v) if v == int(v) else v

def ev(s):
    try: return float(s)
    except Exception: return None

def convert(path, geo_id, anim_prefix):
    d = json.load(open(path))
    groups = {g['uuid']: g for g in d['groups']}
    elements = {e['uuid']: e for e in d['elements']}
    bones = []
    uuid_to_name = {}

    def walk(nodes, parent):
        for n in nodes:
            if isinstance(n, dict):
                g = groups.get(n['uuid'], n)
                name = g['name']
                uuid_to_name[n['uuid']] = name
                b = {'name': name}
                if parent: b['parent'] = parent
                o = g.get('origin', [0, 0, 0])
                b['pivot'] = [num(-o[0]), num(o[1]), num(o[2])]
                r = g.get('rotation', [0, 0, 0])
                if any(r): b['rotation'] = [num(-r[0]), num(-r[1]), num(r[2])]
                gm = bool(g.get('mirror_uv'))
                if gm: b['mirror'] = True
                cubes = []
                for c in n.get('children', []):
                    if isinstance(c, str) and c in elements:
                        e = elements[c]
                        if e.get('type', 'cube') != 'cube': continue
                        f, t = e['from'], e['to']
                        cube = {'origin': [num(-t[0]), num(f[1]), num(f[2])],
                                'size': [num(t[0]-f[0]), num(t[1]-f[1]), num(t[2]-f[2])]}
                        if e.get('inflate'): cube['inflate'] = num(e['inflate'])
                        er = e.get('rotation', [0, 0, 0])
                        if any(er):
                            eo = e.get('origin', [0, 0, 0])
                            cube['pivot'] = [num(-eo[0]), num(eo[1]), num(eo[2])]
                            cube['rotation'] = [num(-er[0]), num(-er[1]), num(er[2])]
                        if e.get('box_uv', d['meta'].get('box_uv')):
                            cube['uv'] = [num(x) for x in e.get('uv_offset', [0, 0])]
                        else:
                            fm = {}
                            for k, fc in e['faces'].items():
                                u = fc['uv']
                                if u is None: continue
                                fm[k] = {'uv': [num(u[0]), num(u[1])], 'uv_size': [num(u[2]-u[0]), num(u[3]-u[1])]}
                            cube['uv'] = fm
                        cm = bool(e.get('mirror_uv'))
                        if cm != gm: cube['mirror'] = cm
                        cubes.append(cube)
                if cubes: b['cubes'] = cubes
                bones.append(b)
                walk([x for x in n.get('children', []) if isinstance(x, dict)], name)
    walk(d['outliner'], None)

    res = d['resolution']
    geo = {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': geo_id, 'texture_width': res['width'], 'texture_height': res['height'],
                        'visible_bounds_width': 4, 'visible_bounds_height': 4, 'visible_bounds_offset': [0, 1.5, 0]},
        'bones': bones}]}

    anims = {}
    for a in d.get('animations', []):
        out = {}
        if a.get('loop') == 'loop': out['loop'] = True
        elif a.get('loop') == 'hold': out['loop'] = 'hold_on_last_frame'
        out['animation_length'] = num(a.get('length', 0))
        bo = {}
        for u, an in a.get('animators', {}).items():
            if an.get('type', 'bone') != 'bone': continue
            name = uuid_to_name.get(u) or an.get('name')
            ch = {}
            for kf in sorted(an.get('keyframes', []), key=lambda k: k['time']):
                c = kf['channel']
                if c not in ('rotation', 'position', 'scale'): continue
                ch.setdefault(c, []).append(kf)
            res_b = {}
            for c, kfs in ch.items():
                def conv(dp):
                    vals = [dp['x'], dp['y'], dp['z']]
                    fl = [ev(v) for v in vals]
                    if any(v is None for v in fl):
                        return [str(v) for v in vals]  # molang
                    if c == 'rotation': fl = [-fl[0], -fl[1], fl[2]]
                    elif c == 'position': fl = [-fl[0], fl[1], fl[2]]
                    return [num(v) for v in fl]
                def point(kf):
                    dps = kf['data_points']
                    cm = kf.get('interpolation') == 'catmullrom'
                    if len(dps) == 1:
                        v = conv(dps[0])
                        return {'post': v, 'lerp_mode': 'catmullrom'} if cm else v
                    pre, post = conv(dps[0]), conv(dps[1])
                    o = {'pre': pre, 'post': post}
                    if cm: o['lerp_mode'] = 'catmullrom'
                    return o
                if len(kfs) == 1 and kfs[0]['time'] == 0 and len(kfs[0]['data_points']) == 1 and kfs[0].get('interpolation') != 'catmullrom':
                    res_b[c] = point(kfs[0])
                elif len(kfs) == 1 and len(kfs[0]['data_points']) == 1 and kfs[0].get('interpolation') != 'catmullrom':
                    res_b[c] = point(kfs[0])
                else:
                    res_b[c] = {str(round(float(k['time']), 4)): point(k) for k in kfs}
                    fk = kfs[0]
                    if fk.get('interpolation') == 'catmullrom' and len(fk['data_points']) == 1 and float(fk['time']) > 0:
                        p = res_b[c][str(round(float(fk['time']), 4))]
                        res_b[c][str(round(float(fk['time']), 4))] = {'pre': p['post'], 'post': p['post'], 'lerp_mode': 'catmullrom'}
            if res_b: bo[name] = res_b
        if bo: out['bones'] = bo
        anims[a['name'] if a['name'].startswith('animation.') else f"{anim_prefix}.{a['name']}"] = out
    return geo, {'format_version': '1.8.0', 'animations': anims}

if __name__ == '__main__':
    geo, an = convert(sys.argv[1], sys.argv[2], sys.argv[3])
    json.dump(geo, open(sys.argv[4], 'w'), indent=1)
    json.dump(an, open(sys.argv[5], 'w'), indent=1)
