// Renders the listing artwork in marketing/images/ from the offline model renders and the bundled OFL fonts.
// Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
// Inputs: marketing/source/renders/ (python3 tools/assets/marketing_renders.py) and the generated mod textures.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const renders = path.join(__dirname, 'renders');
const fonts = path.join(__dirname, 'fonts');
const textures = path.join(root, '1.21.1', 'src', 'main', 'resources', 'assets', 'remnants_ballistics', 'textures');
const url = (file) => 'file://' + file;
const render = (name) => url(path.join(renders, name));
const icon = (name) => url(path.join(textures, 'item', 'icon', name + '.png'));
const item = (name) => url(path.join(textures, 'item', name + '.png'));
const gui = (name) => url(path.join(textures, 'gui', name));

const css = `
@font-face { font-family: RbDisplay; font-weight: 600; src: url('${url(path.join(fonts, 'barlow-condensed-latin-600-normal.woff2'))}'); }
@font-face { font-family: RbDisplay; font-weight: 700; src: url('${url(path.join(fonts, 'barlow-condensed-latin-700-normal.woff2'))}'); }
@font-face { font-family: RbDisplay; font-weight: 800; src: url('${url(path.join(fonts, 'barlow-condensed-latin-800-normal.woff2'))}'); }
@font-face { font-family: RbDisplay; font-weight: 500; src: url('${url(path.join(fonts, 'barlow-condensed-latin-500-normal.woff2'))}'); }
@font-face { font-family: RbText; font-weight: 400; src: url('${url(path.join(fonts, 'inter-latin-400-normal.woff2'))}'); }
@font-face { font-family: RbText; font-weight: 500; src: url('${url(path.join(fonts, 'inter-latin-500-normal.woff2'))}'); }
@font-face { font-family: RbText; font-weight: 600; src: url('${url(path.join(fonts, 'inter-latin-600-normal.woff2'))}'); }
:root { --bg:#0b0d10; --panel:#14171c; --line:rgba(214,222,232,.16); --text:#d9dee6; --dim:#8b94a1; --hi:#f4f6f9;
        --accent:#e2742a; --accent-hi:#ffa25a; --steel:#58606c; }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: RbText, sans-serif; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 70% 45%, #1b2028 0%, #0b0d10 72%); }
.grid { position: absolute; inset: 0; background-image:
          linear-gradient(rgba(140,160,190,.06) 1px, transparent 1px), linear-gradient(90deg, rgba(140,160,190,.06) 1px, transparent 1px);
        background-size: 40px 40px; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 50%, rgba(0,0,0,.7) 100%); }
.display { font-family: RbDisplay, sans-serif; font-weight: 800; text-transform: uppercase; letter-spacing: .02em; color: var(--hi); }
.accent { color: var(--accent); }
.kicker { font-family: RbDisplay, sans-serif; font-weight: 700; color: var(--accent); letter-spacing: .28em; font-size: 22px; text-transform: uppercase; }
.kicker::before { content: ''; display: inline-block; width: 34px; height: 3px; background: var(--accent); margin: 0 14px 6px 0; vertical-align: middle; }
.rule { height: 2px; background: linear-gradient(90deg, var(--accent), rgba(226,116,38,0)); }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 30px; margin: 0 0 16px; font-size: 25px; line-height: 1.36; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 2px; top: 12px; width: 12px; height: 3px; background: var(--accent); }
b.hi { color: var(--hi); font-weight: 600; }
.brackets { position: absolute; pointer-events: none; }
.brackets::before, .brackets::after { content: ''; position: absolute; width: 34px; height: 34px; border: 3px solid var(--accent); }
.brackets::before { left: -10px; top: -10px; border-right: 0; border-bottom: 0; }
.brackets::after { right: -10px; bottom: -10px; border-left: 0; border-top: 0; }
.frame { border: 1px solid rgba(214,222,232,.3); box-shadow: 0 30px 80px rgba(0,0,0,.65); background: #000; }
.frame img { display: block; width: 100%; }
.tag { position: absolute; font-family: RbDisplay, sans-serif; font-weight: 700; font-size: 20px; letter-spacing: .14em; text-transform: uppercase;
       color: var(--hi); background: rgba(11,13,16,.88); border-left: 3px solid var(--accent); padding: 6px 12px 4px; white-space: nowrap; }
.pixel { image-rendering: pixelated; }
.chip { display: inline-block; font-family: RbDisplay, sans-serif; font-weight: 600; font-size: 19px; letter-spacing: .06em; padding: 3px 10px 2px;
        border: 1px solid var(--line); background: rgba(255,255,255,.04); margin: 0 6px 6px 0; color: var(--text); }
.chip.good { border-color: rgba(98,200,120,.55); color: #8fe0a2; }
.chip.bad { border-color: rgba(230,96,80,.55); color: #f0998c; }
.mono { font-family: ui-monospace, 'DejaVu Sans Mono', monospace; }
`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:26px;
              background:linear-gradient(90deg,rgba(0,0,0,0),rgba(16,19,24,.92) 26%,rgba(16,19,24,.92) 74%,rgba(0,0,0,0))">
    <div class="rule" style="width:280px;transform:scaleX(-1)"></div>
    <div style="width:10px;height:10px;background:var(--accent);transform:rotate(45deg)"></div>
    <div class="display" style="font-size:52px;white-space:nowrap">${title}</div>
    <div style="width:10px;height:10px;background:var(--accent);transform:rotate(45deg)"></div>
    <div class="rule" style="width:280px"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right.
const card = ({ kicker, title, points, art, width = 1600, height = 900, copyWidth = 560 }) => page(width, height, `
  <div class="grid"></div><div class="vignette"></div>
  <div style="position:absolute;left:80px;top:0;bottom:0;width:${copyWidth}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker">${kicker}</div>
    <div class="display" style="font-size:66px;line-height:1.0;margin:20px 0 24px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((p) => `<li>${p.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;left:${copyWidth + 130}px;right:60px;top:60px;bottom:60px">${art}</div>`);

const WEAPONS = [
  ['r9_sidearm', 'R-9 Sidearm'], ['ironfang_50', 'Ironfang .50'], ['warden_357', 'Warden .357'], ['viper_9', 'Viper-9'],
  ['spectre_pdw', 'Spectre PDW'], ['arx_16', 'ARX-16'], ['raven_ak', 'Raven AK'], ['mantis_br', 'Mantis BR'],
  ['breacher_12', 'Breacher-12'], ['thunderpump', 'Thunderpump'], ['ghostline_m24', 'Ghostline M24'], ['longshot_50', 'Longshot .50'],
  ['titan_56', 'Titan-56'], ['bulwark_762', 'Bulwark-762'], ['voidcaster', 'Voidcaster'], ['eclipse_railgun', 'Eclipse Railgun'],
];

const lineup = WEAPONS.map(([id, name]) => `
  <div style="position:relative;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;padding:6px 8px 10px;
              border:1px solid var(--line);background:rgba(255,255,255,.025)">
    <img src="${render('side-' + id + '.png')}" style="max-width:100%;max-height:118px;object-fit:contain">
    <div style="font-family:RbDisplay;font-weight:700;font-size:21px;letter-spacing:.08em;text-transform:uppercase;color:var(--hi);margin-top:8px">${name}</div>
  </div>`).join('');

const ATTACHMENTS = [
  ['tripoint_holo', 'Tripoint Holo', [['ADS spread −12%', 'good'], ['Aim time +8%', 'bad']]],
  ['ranger_scope', 'Ranger 4×', [['ADS spread −20%', 'good'], ['Sway +10%', 'bad']]],
  ['suppressor', 'Suppressor', [['Loudness −75%', 'good'], ['Damage −5%', 'bad']]],
  ['muzzle_brake', 'Muzzle Brake', [['Vertical recoil −20%', 'good'], ['Loudness +20%', 'bad']]],
  ['angled_grip', 'Angled Grip', [['Aim time −15%', 'good'], ['Vertical recoil +5%', 'bad']]],
  ['drum_mag', 'Drum Magazine', [['Capacity +100%', 'good'], ['Reload +35%', 'bad']]],
  ['quick_mag', 'Quick-Swap Mag', [['Reload −25%', 'good'], ['Capacity −10%', 'bad']]],
  ['heavy_stock', 'Heavy Stock', [['Vertical recoil −20%', 'good'], ['Aim time +12%', 'bad']]],
];

const statRow = (label, value, frac, delta) => `
  <div style="display:flex;align-items:center;gap:12px;margin:0 0 9px">
    <div style="width:150px;font-size:16px;color:var(--dim)">${label}</div>
    <div style="flex:1;height:7px;background:#262b33;position:relative"><div style="position:absolute;left:0;top:0;bottom:0;width:${frac * 100}%;background:#5b8fd6"></div></div>
    <div style="width:64px;text-align:right;font-size:16px;color:var(--hi)">${value}</div>
    <div style="width:62px;font-size:15px;color:${delta.startsWith('−') ? '#8fe0a2' : delta.startsWith('+') ? '#f0998c' : 'var(--dim)'}">${delta}</div>
  </div>`;

// Gunsmith Workbench mock-up built from the mod's real GUI texture and item icons (slot positions from GunsmithMenu).
const workbench = () => {
  const s = 3;
  const slot = (x, y, ic) => `<img class="pixel" src="${icon(ic)}" style="position:absolute;left:${x * s}px;top:${y * s}px;width:${16 * s}px;height:${16 * s}px">`;
  return `<div style="position:relative;width:${176 * s}px;height:${166 * s}px">
    <img class="pixel" src="${gui('container/gunsmith.png')}" style="position:absolute;left:0;top:0;width:${256 * s}px;height:${256 * s}px;clip-path:inset(0 ${80 * s}px ${90 * s}px 0)">
    ${slot(80, 40, 'arx_16')}${slot(80, 18, 'tripoint_holo')}${slot(116, 40, 'suppressor')}${slot(98, 62, 'vertical_grip')}${slot(44, 40, 'tactical_stock')}
    <div style="position:absolute;left:${8 * s}px;top:${6 * s}px;font-family:RbDisplay;font-weight:700;font-size:20px;line-height:1;color:#404040">GUNSMITH WORKBENCH</div>
  </div>`;
};

const assets = {
  banner: () => page(1800, 600, `
    <div style="position:absolute;inset:0;background:url('${render('fp-arx_16-hip.jpg')}') center/cover;filter:blur(14px) brightness(.32) saturate(1.1);transform:scale(1.08)"></div>
    <div class="grid"></div><div class="vignette"></div>
    <img src="${render('build-arx_16.png')}" style="position:absolute;right:30px;top:60px;width:900px;filter:drop-shadow(0 30px 40px rgba(0,0,0,.7))">
    <img src="${render('hero-ghostline_m24.png')}" style="position:absolute;right:430px;bottom:24px;width:560px;opacity:.96;filter:drop-shadow(0 20px 30px rgba(0,0,0,.7))">
    <div style="position:absolute;left:90px;top:120px">
      <div class="kicker">NeoForge · Minecraft 1.21.1</div>
      <div class="display" style="font-size:150px;line-height:.9;margin-top:22px">Remnants</div>
      <div class="display accent" style="font-size:96px;line-height:.95;letter-spacing:.12em">Ballistics</div>
      <div style="font-size:27px;color:var(--text);margin-top:26px;max-width:640px;line-height:1.35">
        16 weapons · gunsmithing · server-authoritative ballistics built for multiplayer</div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 46%,#232a34 0%,#0b0d10 70%)"></div>
    <div class="grid" style="background-size:32px 32px"></div>
    <svg width="512" height="512" style="position:absolute;inset:0">
      <circle cx="256" cy="256" r="190" fill="none" stroke="#e2742a" stroke-width="10"/>
      <circle cx="256" cy="256" r="160" fill="none" stroke="rgba(226,116,38,.35)" stroke-width="3"/>
      ${[0, 90, 180, 270].map((a) => `<line x1="256" y1="40" x2="256" y2="96" stroke="#e2742a" stroke-width="10" transform="rotate(${a} 256 256)"/>`).join('')}
    </svg>
    <img src="${render('hero-arx_16.png')}" style="position:absolute;left:36px;top:160px;width:440px;filter:drop-shadow(0 18px 24px rgba(0,0,0,.8))">
`),

  divider: () => page(1400, 40, `
    <div style="position:absolute;left:150px;right:150px;top:19px;height:2px;background:linear-gradient(90deg,rgba(226,116,38,0),#e2742a 30%,#e2742a 70%,rgba(226,116,38,0))"></div>
    <div style="position:absolute;left:692px;top:12px;width:16px;height:16px;background:#e2742a;transform:rotate(45deg)"></div>`, true),

  'card-arsenal': () => card({
    kicker: 'The arsenal', title: 'Sixteen weapons,<br>seven classes',
    points: [
      'Pistols, a revolver, SMGs, assault rifles, shotguns, snipers, LMGs and two experimental prototypes',
      'Semi, burst and full-auto with per-weapon recoil, spread, falloff and bullet drop',
      'Tactical and empty reloads, single-round loading, pump and bolt cycling',
      'Hand-built voxel models with up to 118 elements each',
    ],
    copyWidth: 520,
    art: `<div style="display:grid;grid-template-columns:repeat(4,1fr);grid-auto-rows:1fr;gap:12px;height:100%">${lineup}</div>`,
  }),

  'card-handling': () => card({
    kicker: 'First person', title: 'Aim down<br>real sights',
    points: [
      'The sight picture lines up exactly with the crosshair',
      'Reflex and holographic reticles; full-screen 4× and 8× scopes',
      'Hip fire, movement, jumping and sustained fire all cost accuracy',
      'Per-weapon animations: equip, inspect, reloads, bolt and pump',
    ],
    art: `<div class="frame" style="position:absolute;left:0;top:30px;width:880px"><img src="${render('fp-arx_16-ads.jpg')}"></div>
          <div class="frame" style="position:absolute;right:0;bottom:30px;width:420px"><img src="${render('fp-arx_16-hip.jpg')}"></div>
          <div class="tag" style="left:20px;top:50px">Aiming</div><div class="tag" style="right:300px;bottom:216px">Hip fire</div>`,
  }),

  'card-ballistics': () => card({
    kicker: 'Ballistics', title: 'The server scores<br>every shot',
    points: [
      'Hitscan with bullet drop, damage falloff and head, body and leg zones',
      'Rounds punch through leaves, panes and thin wood, losing damage',
      'Shallow-angle ricochets; glass shatters',
      'Lag-compensated player hitboxes, up to 250 ms by default',
    ],
    art: `<svg viewBox="0 0 860 780" width="100%" height="100%">
      <defs><marker id="a" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto"><path d="M0,0 L10,5 L0,10 z" fill="#e2742a"/></marker></defs>
      <rect x="0" y="0" width="860" height="780" fill="rgba(255,255,255,.02)" stroke="rgba(214,222,232,.16)"/>
      <text x="30" y="52" fill="#8b94a1" font-family="RbDisplay" font-weight="700" font-size="22" letter-spacing="3">TRAJECTORY</text>
      <path d="M40,150 Q430,118 820,214" fill="none" stroke="#e2742a" stroke-width="4" marker-end="url(#a)"/>
      <line x1="40" y1="150" x2="820" y2="150" stroke="rgba(214,222,232,.25)" stroke-dasharray="8 8"/>
      <text x="640" y="246" fill="#d9dee6" font-family="RbText" font-size="20">bullet drop</text>
      <text x="30" y="330" fill="#8b94a1" font-family="RbDisplay" font-weight="700" font-size="22" letter-spacing="3">PENETRATION · RICOCHET</text>
      <rect x="250" y="370" width="70" height="160" fill="#4f8a3b" opacity=".85"/><text x="235" y="560" fill="#8b94a1" font-family="RbText" font-size="18">leaves</text>
      <rect x="560" y="370" width="70" height="200" fill="#6b7078"/><text x="560" y="600" fill="#8b94a1" font-family="RbText" font-size="18">stone</text>
      <path d="M40,420 L250,440 M320,447 L560,470" fill="none" stroke="#e2742a" stroke-width="4"/>
      <path d="M560,470 L760,400" fill="none" stroke="#ffa25a" stroke-width="4" stroke-dasharray="10 7" marker-end="url(#a)"/>
      <text x="330" y="430" fill="#d9dee6" font-family="RbText" font-size="18">−damage per block</text>
      <text x="650" y="392" fill="#d9dee6" font-family="RbText" font-size="18">ricochet</text>
      <text x="30" y="640" fill="#8b94a1" font-family="RbDisplay" font-weight="700" font-size="22" letter-spacing="3">HIT ZONES (DEFAULTS)</text>
      <g font-family="RbDisplay" font-weight="700" font-size="30">
        <rect x="40" y="670" width="240" height="70" fill="rgba(226,116,38,.18)" stroke="#e2742a"/><text x="62" y="716" fill="#f4f6f9">HEAD ×2.0</text>
        <rect x="310" y="670" width="240" height="70" fill="rgba(255,255,255,.05)" stroke="rgba(214,222,232,.4)"/><text x="332" y="716" fill="#f4f6f9">BODY ×1.0</text>
        <rect x="580" y="670" width="240" height="70" fill="rgba(255,255,255,.05)" stroke="rgba(214,222,232,.4)"/><text x="602" y="716" fill="#f4f6f9">LEGS ×0.75</text>
      </g></svg>`,
  }),

  'card-gunsmith': () => card({
    kicker: 'Gunsmithing', title: 'Build it<br>your way',
    points: [
      'Six slots: optic, muzzle, underbarrel, magazine, stock and finish',
      'A live 3D preview and stat panel show every change before you commit',
      'Attachments live on the weapon and render in first and third person',
      'Duplication-safe on multiplayer servers',
    ],
    art: `<img src="${render('build-arx_16.png')}" style="position:absolute;right:-10px;top:0;width:640px;filter:drop-shadow(0 20px 30px rgba(0,0,0,.7))">
      <div style="position:absolute;left:0;bottom:0;box-shadow:0 30px 70px rgba(0,0,0,.7)">${workbench()}</div>
      <div style="position:absolute;right:0;bottom:0;width:330px;padding:20px 20px 12px;background:rgba(20,23,28,.96);border:1px solid var(--line)">
        <div style="font-family:RbDisplay;font-weight:700;font-size:26px;color:#8fe0a2;margin-bottom:12px;letter-spacing:.05em">ARX-16</div>
        ${statRow('Vertical recoil', '0.98°', 0.33, '−25%')}${statRow('Aimed spread', '0.22°', 0.2, '−12%')}${statRow('Aim time', '7 t', 0.5, '+18%')}
        ${statRow('Loudness', '20', 0.15, '−75%')}${statRow('Damage', '5.2', 0.55, '−5%')}
      </div>`,
  }),

  'card-attachments': () => card({
    kicker: 'Attachments', title: '23 attachments,<br>real trade-offs',
    points: [
      '4 optics, 5 muzzle devices, 3 grips, 3 magazines and 3 stocks',
      'Five finishes: desert and arctic camo, crimson, gilded and void',
      'Compatibility rules per weapon class',
      'Full stats in every tooltip',
    ],
    copyWidth: 500,
    art: `<div style="display:grid;grid-template-columns:repeat(4,1fr);grid-template-rows:repeat(2,1fr);gap:14px;height:100%">
      ${ATTACHMENTS.map(([id, name, chips]) => `<div style="display:flex;flex-direction:column;border:1px solid var(--line);background:rgba(255,255,255,.025);padding:14px">
        <div style="flex:1;display:flex;align-items:center;justify-content:center;min-height:0"><img src="${render('att-' + id + '.png')}" style="max-width:100%;max-height:170px;object-fit:contain"></div>
        <div style="font-family:RbDisplay;font-weight:700;font-size:24px;letter-spacing:.06em;text-transform:uppercase;color:var(--hi);margin:8px 0 6px">${name}</div>
        <div>${chips.map(([t, k]) => `<span class="chip ${k}">${t}</span>`).join('')}</div></div>`).join('')}</div>`,
  }),

  'card-finishes': () => card({
    kicker: 'Finishes', title: 'Five weapon<br>finishes',
    points: [
      'Recolour any weapon at the Gunsmith Workbench',
      'Desert and arctic camo, crimson, gilded and void',
      'Found as loot in themed structures, or crafted',
      'Purely cosmetic: no stat changes',
    ],
    art: `<div style="position:relative;height:100%">
      ${['desert', 'arctic', 'crimson', 'gilded', 'void'].map((s, i) => `<img src="${render('skin-arx_16-' + s + '.png')}"
        style="position:absolute;left:${(i % 2) * 330 + (i >= 4 ? 165 : 0)}px;top:${Math.floor(i / 2) * 250 + 10}px;width:520px;filter:drop-shadow(0 18px 24px rgba(0,0,0,.7))">
        <div class="tag" style="left:${(i % 2) * 330 + (i >= 4 ? 165 : 0) + 20}px;top:${Math.floor(i / 2) * 250 + 200}px">${s}</div>`).join('')}</div>`,
  }),

  'card-prototypes': () => card({
    kicker: 'Prototypes', title: 'Voidcaster &amp;<br>Eclipse Railgun',
    points: [
      '<b class="hi">Voidcaster</b>: fires void orbs instead of bullets, semi or full-auto',
      '<b class="hi">Eclipse Railgun</b>: hold to charge for a second, then a 30-damage rail shot that pierces targets and soft cover',
      'Powered by Energy Cells from the Ammunition Press',
      'Built from remnant alloy, void cores and prototype blueprints',
    ],
    art: `<img src="${render('hero-eclipse_railgun.png')}" style="position:absolute;right:0;top:0;width:820px;filter:drop-shadow(0 24px 30px rgba(0,0,0,.7))">
          <img src="${render('hero-voidcaster.png')}" style="position:absolute;left:0;bottom:10px;width:640px;filter:drop-shadow(0 24px 30px rgba(0,0,0,.7))">`,
  }),

  'card-progression': () => {
    const node = (img, label, pixel = true) => `<div style="display:flex;flex-direction:column;align-items:center;width:150px">
      <div style="width:104px;height:104px;border:1px solid var(--line);background:rgba(255,255,255,.04);display:flex;align-items:center;justify-content:center">
        <img class="${pixel ? 'pixel' : ''}" src="${img}" style="width:96px;height:96px"></div>
      <div style="font-family:RbDisplay;font-weight:700;font-size:19px;letter-spacing:.06em;text-transform:uppercase;text-align:center;margin-top:8px;color:var(--hi)">${label}</div></div>`;
    const arrow = '<div style="flex:0 0 40px;height:3px;background:var(--accent);margin-top:-30px"></div>';
    const row = (items) => `<div style="display:flex;align-items:center;gap:6px;margin-bottom:36px">${items.join(arrow)}</div>`;
    return card({
      kicker: 'Progression', title: 'From steel<br>to void cores',
      points: [
        'Smelt steel, alloy brass, press your own ammunition',
        'Five weapon tiers; blueprints from outposts, strongholds, bastions and ancient cities unlock the top three',
        'Blueprints are never consumed, and can be copied for friends',
        'Loot in 25 vanilla structure and mob tables',
      ],
      art: `<div style="padding:20px 0 0 10px">
        ${row([node(item('steel_ingot'), 'Steel'), node(item('gun_parts'), 'Gun parts'), node(icon('r9_sidearm'), 'Tier 1–2'), node(icon('arx_16'), 'ARX-16')])}
        ${row([node(item('blueprint_advanced'), 'Blueprint'), node(item('precision_parts'), 'Precision'), node(icon('mantis_br'), 'Tier 3'), node(icon('titan_56'), 'Titan-56')])}
        ${row([node(item('blueprint_prototype'), 'Prototype'), node(item('remnant_alloy'), 'Alloy'), node(item('void_core'), 'Void core'), node(icon('eclipse_railgun'), 'Tier 5')])}
        ${row([node(item('casing'), 'Casings'), node(item('ammo_556'), '5.56'), node(item('ammo_12g'), '12 gauge'), node(item('energy_cell'), 'Cells')])}
      </div>`,
    });
  },

  'card-multiplayer': () => card({
    kicker: 'Multiplayer', title: 'Built for<br>servers',
    points: [
      'The server owns ammo, timing, spread, hits and reloads',
      'Clients predict every shot, so firing feels instant',
      'A shared spread seed: the server rolls the pellets you saw',
      'Replay protection, rate limiting, aim clamping and a spread floor',
    ],
    art: `<svg viewBox="0 0 860 780" width="100%" height="100%" font-family="RbDisplay" font-weight="700">
      <defs><marker id="m" markerWidth="10" markerHeight="10" refX="8" refY="5" orient="auto"><path d="M0,0 L10,5 L0,10 z" fill="#e2742a"/></marker></defs>
      <rect x="20" y="40" width="300" height="700" fill="rgba(255,255,255,.03)" stroke="rgba(214,222,232,.3)"/>
      <rect x="540" y="40" width="300" height="700" fill="rgba(226,116,38,.06)" stroke="#e2742a"/>
      <text x="170" y="90" fill="#f4f6f9" font-size="34" text-anchor="middle" letter-spacing="3">CLIENT</text>
      <text x="690" y="90" fill="#f4f6f9" font-size="34" text-anchor="middle" letter-spacing="3">SERVER</text>
      ${[[150, 'Predict shot, recoil, FX', 'Validate slot, ammo, timing', 'FIRE · seq 41'],
         [330, 'Hit marker', 'Trace with lag compensation', 'HIT CONFIRM'],
         [510, 'Remote shots animated', 'Broadcast tracers, impacts', 'SHOT EFFECTS'],
         [670, 'Resync magazine', 'Reject replay / too fast', 'CORRECTION']].map(([y, c, s, label], i) => `
        <text x="40" y="${y}" fill="#d9dee6" font-family="RbText" font-weight="500" font-size="21">${c}</text>
        <text x="560" y="${y}" fill="#d9dee6" font-family="RbText" font-weight="500" font-size="21">${s}</text>
        <line x1="${i === 0 ? 330 : 530}" y1="${y + 30}" x2="${i === 0 ? 530 : 330}" y2="${y + 30}" stroke="#e2742a" stroke-width="3" marker-end="url(#m)"/>
        <text x="430" y="${y + 20}" fill="#ffa25a" font-size="20" text-anchor="middle" letter-spacing="2">${label}</text>`).join('')}
    </svg>`,
  }),

  'card-config': () => card({
    kicker: 'Configuration', title: 'Tune every<br>number',
    points: [
      'Client, common and server files with an in-game editor',
      'Per-weapon damage, fire rate, magazine and reload overrides',
      'Toggle recipes, loot, glass breaking, penetration and ricochet',
      'Accessibility: flash brightness, camera shake, high-contrast HUD',
    ],
    art: `<div class="mono" style="position:absolute;inset:20px 0;padding:34px 40px;background:#0f1216;border:1px solid var(--line);font-size:24px;line-height:1.55;color:#c6ccd6;white-space:pre">
<span style="color:#8b94a1"># remnants_ballistics-server.toml</span>
<span style="color:#ffa25a">[combat]</span>
damageMultiplier = <span style="color:#8fe0a2">1.0</span>
headshotMultiplier = <span style="color:#8fe0a2">2.0</span>
limbMultiplier = <span style="color:#8fe0a2">0.75</span>
<span style="color:#ffa25a">[network]</span>
lagCompensation = <span style="color:#8fe0a2">true</span>
maxLagCompensationMs = <span style="color:#8fe0a2">250</span>
<span style="color:#ffa25a">[weapons.arx_16]</span>
enabled = <span style="color:#8fe0a2">true</span>
damage = <span style="color:#8fe0a2">5.5</span>
fireRate = <span style="color:#8fe0a2">720</span>
magazine = <span style="color:#8fe0a2">30</span></div>`,
  }),
};

const headers = {
  'header-arsenal': 'The Arsenal', 'header-shooting': 'Shooting', 'header-gunsmithing': 'Gunsmithing',
  'header-progression': 'Progression', 'header-multiplayer': 'Multiplayer', 'header-config': 'Configuration',
  'header-compatibility': 'Downloads &amp; Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery',
};
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);

const sizes = { banner: [1800, 600], icon: [512, 512], divider: [1400, 40] };
const sizeOf = (name) => sizes[name] || (name.startsWith('header-') ? [1400, 120] : [1600, 900]);

(async () => {
  const wanted = process.argv.slice(2);
  const names = wanted.length ? wanted : Object.keys(assets);
  fs.mkdirSync(out, { recursive: true });
  const browser = await chromium.launch();
  const tmp = fs.mkdtempSync(path.join(require('os').tmpdir(), 'rb-marketing-'));
  for (const name of names) {
    if (!assets[name]) throw new Error(`Unknown asset ${name}`);
    const [width, height] = sizeOf(name);
    const html = path.join(tmp, name + '.html');
    fs.writeFileSync(html, assets[name]());
    const pg = await browser.newPage({ viewport: { width, height } });
    await pg.goto(url(html));
    await pg.evaluate(() => document.fonts.ready);
    await pg.waitForTimeout(150);
    const transparent = name.startsWith('header-') || name === 'divider';
    await pg.screenshot({ path: path.join(out, name + '.png'), omitBackground: transparent });
    await pg.close();
    console.log('rendered', name);
  }
  await browser.close();
  fs.rmSync(tmp, { recursive: true, force: true });
})().catch((e) => { console.error(e); process.exit(1); });
