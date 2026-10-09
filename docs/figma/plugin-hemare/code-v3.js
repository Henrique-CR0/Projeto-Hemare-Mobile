// =====================================================================
// Hemare Mobile v3 — design fiel ao app rodando (Pixel, 411×923 dp).
// Medidas tiradas do app real no emulador (uiautomator) + código Compose.
// v3: paleta escura quente, cabeçalho sem o espaço duplo da barra de status e com
// seta de voltar, hub do hospital (Estoque, Histórico, Perfil, Plano), 4 integrantes.
// =====================================================================

const W = 411;      // largura da tela (dp)
const H = 923;      // altura da tela (dp)
const SB_H = 54;    // barra de status
const NAV_Y = 819;  // topo da barra de navegação (80) + área de gestos (24)

const HEX = (s) => {
  const n = parseInt(s.replace('#', ''), 16);
  return { r: ((n >> 16) & 255) / 255, g: ((n >> 8) & 255) / 255, b: (n & 255) / 255 };
};
const SOLIDO = (hex, opacity) => {
  const p = { type: 'SOLID', color: HEX(hex) };
  if (opacity != null) p.opacity = opacity;
  return p;
};

// ---------------------------------------------------------------- fontes
let FONTE = 'Roboto';
const F = {};
async function prepararFontes() {
  figma.skipInvisibleInstanceChildren = false;
  const fontes = await figma.listAvailableFontsAsync();
  const tem = (fam, st) => fontes.some((f) => f.fontName.family === fam && f.fontName.style === st);
  if (!tem('Roboto', 'Regular')) FONTE = 'Inter';
  const escolher = (...estilos) => {
    for (const s of estilos) if (tem(FONTE, s)) return { family: FONTE, style: s };
    return { family: FONTE, style: 'Regular' };
  };
  F.regular = escolher('Regular');
  F.medium = escolher('Medium', 'Regular');
  F.semibold = escolher('SemiBold', 'Semi Bold', 'Medium', 'Bold');
  F.bold = escolher('Bold');
  F.italicSemi = escolher('SemiBold Italic', 'Semi Bold Italic', 'Medium Italic', 'Italic');
  for (const k of Object.keys(F)) await figma.loadFontAsync(F[k]);
}

// ---------------------------------------------------------------- cores
const CORES = [
  ['marca/vermelho', '#C8102E', '#C8102E'],
  ['marca/vermelho-acao', '#E8112D', '#E8112D'],
  ['marca/amarelo-alerta', '#E0B000', '#E0B000'],
  ['marca/verde', '#35C47A', '#35C47A'],
  ['marca/rosa-claro', '#FFD9DF', '#FFD9DF'],
  ['marca/roxo-emergencia', '#7C4DFF', '#7C4DFF'],
  ['marca/branco', '#FFFFFF', '#FFFFFF'],
  ['tema/primary', '#C8102E', '#E8112D'],
  ['tema/on-primary', '#FFFFFF', '#FFFFFF'],
  ['tema/background', '#FFF5F5', '#170F10'],
  ['tema/on-background', '#2B0D10', '#F5EDEC'],
  ['tema/surface', '#FFFFFF', '#231719'],
  ['tema/on-surface', '#2B0D10', '#F5EDEC'],
  ['tema/surface-variant', '#FFD9DF', '#2F2023'],
  ['tema/on-surface-variant', '#8A6B6F', '#B3A0A2'],
  ['tema/outline', '#79747E', '#938F99'],
  ['tema/outline-variant', '#CAC4D0', '#49454F'],
  ['tema/switch-off', '#E6E0E9', '#36343B'],
];
const V = {};
const ESCURO_DE = {};
let COL, COL_ESCURO, MODO_CLARO, MODO_ESCURO;
let MODOS_OK = true;

function criarVariaveis() {
  COL = figma.variables.createVariableCollection('Hemare / Cores');
  MODO_CLARO = COL.modes[0].modeId;
  COL.renameMode(MODO_CLARO, 'Claro');
  try { MODO_ESCURO = COL.addMode('Escuro'); } catch (e) {
    MODOS_OK = false;
    COL_ESCURO = figma.variables.createVariableCollection('Hemare / Cores (Escuro)');
  }
  for (const [nome, claro, escuro] of CORES) {
    const v = figma.variables.createVariable(nome, COL, 'COLOR');
    v.scopes = ['ALL_FILLS', 'STROKE_COLOR'];
    v.setValueForMode(MODO_CLARO, Object.assign(HEX(claro), { a: 1 }));
    if (MODOS_OK) v.setValueForMode(MODO_ESCURO, Object.assign(HEX(escuro), { a: 1 }));
    else {
      const ve = figma.variables.createVariable(nome, COL_ESCURO, 'COLOR');
      ve.scopes = ['ALL_FILLS', 'STROKE_COLOR'];
      ve.setValueForMode(COL_ESCURO.modes[0].modeId, Object.assign(HEX(escuro), { a: 1 }));
      ESCURO_DE[v.id] = ve;
    }
    V[nome.split('/')[1]] = v;
  }
}

// Cores translúcidas são sempre da marca (fixas): pintura sólida comum,
// porque o Figma ignora a opacidade de pinturas ligadas a variáveis.
// Card do Material 3 com elevation 1.5 dp.
const SOMBRA = { type: 'DROP_SHADOW', color: { r: 0, g: 0, b: 0, a: 0.18 }, offset: { x: 0, y: 1 }, radius: 3, spread: 0, visible: true, blendMode: 'NORMAL' };
function sombra(no) { try { no.effects = [SOMBRA]; } catch (e) { /* estético */ } }

function tinta(v, opacity) {
  if (opacity != null && opacity < 1) {
    const linha = CORES.find((c) => c[0] === v.name);
    return SOLIDO(linha ? linha[1] : '#FFFFFF', opacity);
  }
  return figma.variables.setBoundVariableForPaint({ type: 'SOLID', color: { r: 0, g: 0, b: 0 } }, 'color', v);
}

function aplicarTema(no, tema) {
  if (MODOS_OK) {
    const modo = tema === 'escuro' ? MODO_ESCURO : MODO_CLARO;
    try { no.setExplicitVariableModeForCollection(COL, modo); } catch (e) { no.setExplicitVariableModeForCollection(COL.id, modo); }
    return;
  }
  if (tema !== 'escuro') return;
  for (const n of [no].concat(no.findAll(() => true))) {
    for (const campo of ['fills', 'strokes']) {
      if (!(campo in n) || !Array.isArray(n[campo])) continue;
      let mudou = false;
      const novos = n[campo].map((p) => {
        const id = p.boundVariables && p.boundVariables.color && p.boundVariables.color.id;
        if (id && ESCURO_DE[id]) { mudou = true; return figma.variables.setBoundVariableForPaint(p, 'color', ESCURO_DE[id]); }
        return p;
      });
      if (mudou) try { n[campo] = novos; } catch (e) { /* ok */ }
    }
  }
}

// ---------------------------------------------------------------- texto
// [nome, peso, tamanho, alturaLinha, espaçamento, recorte]
// recorte = bodyLarge do Type.kt: o Compose corta a meia-entrelinha (1 linha = 19dp).
const ESTILOS = {
  emoji48: ['Emoji/48', 'regular', 48, 57, 0],
  destaque: ['Destaque/Título Início', 'bold', 28, 28.5, 0.5],
  headline: ['Título/Tela', 'bold', 24, 32, 0],
  headlineSmall: ['Título/Número', 'bold', 24, 32, 0],
  cabecalho: ['Título/Cabeçalho', 'bold', 22, 26, 0.5],
  titleLarge: ['Título/Grande', 'bold', 22, 28, 0],
  titleMedium: ['Título/Médio', 'bold', 16, 24, 0.15],
  titleItalic: ['Título/Médio Itálico', 'italicSemi', 16, 24, 0.15],
  bodyLarge: ['Corpo/Grande', 'regular', 16, 24, 0.5, true],
  bodyLargeBold: ['Corpo/Grande Negrito', 'bold', 16, 24, 0.5, true],
  bodyLargeSemi: ['Corpo/Grande Semi', 'semibold', 16, 24, 0.5, true],
  bodyMedium: ['Corpo/Médio', 'regular', 14, 20, 0.25],
  bodyMediumBold: ['Corpo/Médio Negrito', 'bold', 14, 20, 0.25],
  bodySmall: ['Corpo/Pequeno', 'regular', 12, 16, 0.4],
  bodySmallBold: ['Corpo/Pequeno Negrito', 'bold', 12, 16, 0.4],
  labelLarge: ['Rótulo/Grande', 'medium', 14, 20, 0.1],
  labelLargeBold: ['Rótulo/Grande Negrito', 'bold', 14, 20, 0.1],
  labelMedium: ['Rótulo/Médio', 'medium', 12, 16, 0.5],
  labelMediumBold: ['Rótulo/Médio Negrito', 'bold', 12, 16, 0.5],
  labelSmall: ['Rótulo/Pequeno', 'medium', 11, 16, 0.5],
  padrao20: ['Padrão/20 Médio', 'medium', 20, 24, 0.5],
  padrao16: ['Padrão/16', 'regular', 16, 24, 0.5],
  padrao15: ['Padrão/15 Médio', 'medium', 15, 24, 0.5],
  padrao15b: ['Padrão/15 Negrito', 'bold', 15, 24, 0.5],
  padrao13: ['Padrão/13', 'regular', 13, 24, 0.5],
  padrao10: ['Padrão/10', 'regular', 10, 24, 0.5],
  padraoEmoji20: ['Padrão/Emoji 20', 'regular', 20, 24, 0.5],
};
const TS = {};
const RECORTE = new Set();

function criarEstilos() {
  for (const k of Object.keys(ESTILOS)) {
    const [nome, peso, tam, lh, ls] = ESTILOS[k];
    const s = figma.createTextStyle();
    s.name = 'Hemare/' + nome;
    s.fontName = F[peso];
    s.fontSize = tam;
    s.lineHeight = { value: lh, unit: 'PIXELS' };
    s.letterSpacing = { value: ls, unit: 'PIXELS' };
    TS[k] = s;
  }
}

function ehVariavel(c) { return c && typeof c === 'object' && 'resolvedType' in c; }

async function texto(str, estilo, cor, o) {
  o = o || {};
  const e = ESTILOS[estilo];
  const t = figma.createText();
  t.fontName = F[e[1]];
  t.fontSize = e[2];
  t.lineHeight = { value: e[3], unit: 'PIXELS' };
  t.letterSpacing = { value: e[4], unit: 'PIXELS' };
  t.characters = str;
  try { await t.setTextStyleIdAsync(TS[estilo].id); } catch (err) { /* sem estilo */ }
  if (cor) t.fills = [ehVariavel(cor) ? tinta(cor) : cor];
  if (o.align) t.textAlignHorizontal = o.align;
  t.name = o.nome || str.slice(0, 48);
  if (e[5]) RECORTE.add(t.id);
  return t;
}

async function textoDoc(str, tam, peso, hex) {
  const t = figma.createText();
  t.fontName = F[peso || 'regular'];
  t.fontSize = tam;
  t.characters = str;
  t.fills = [SOLIDO(hex || '#2B0D10')];
  return t;
}

// Recorte do bodyLarge: altura = 24·linhas − 5, texto centralizado.
function recortar(t) {
  if (!RECORTE.has(t.id)) return;
  const linhas = Math.max(1, Math.round(t.height / 24));
  t.textAutoResize = 'NONE';
  t.resize(Math.max(1, t.width), 24 * linhas - 5);
  t.textAlignVertical = 'CENTER';
}

// ---------------------------------------------------------------- layout
function caixa(nome, o) {
  o = o || {};
  const f = figma.createFrame();
  f.name = nome;
  f.fills = o.fill ? (Array.isArray(o.fill) ? o.fill : [o.fill]) : [];
  f.clipsContent = !!o.clip;
  if (o.dir !== 'NONE') {
    f.layoutMode = o.dir === 'H' ? 'HORIZONTAL' : 'VERTICAL';
    f.primaryAxisSizingMode = 'AUTO';
    f.counterAxisSizingMode = 'AUTO';
    f.itemSpacing = o.gap || 0;
    let p = o.pad == null ? [0, 0, 0, 0] : o.pad;
    if (typeof p === 'number') p = [p, p, p, p];
    else if (p.length === 2) p = [p[0], p[1], p[0], p[1]];
    f.paddingTop = p[0]; f.paddingRight = p[1]; f.paddingBottom = p[2]; f.paddingLeft = p[3];
    if (o.main) f.primaryAxisAlignItems = o.main;
    if (o.cross) f.counterAxisAlignItems = o.cross;
  }
  if (o.radius) f.cornerRadius = o.radius;
  if (o.stroke) { f.strokes = [o.stroke]; f.strokeWeight = o.strokeW || 1; f.strokeAlign = 'INSIDE'; }
  return f;
}

function bloco(pai, nome, o, h, v) {
  o = o || {};
  const f = caixa(nome, o);
  pai.appendChild(f);
  if (h) f.layoutSizingHorizontal = h;
  if (v) f.layoutSizingVertical = v;
  if (o.wrap) { f.layoutWrap = 'WRAP'; f.counterAxisSpacing = o.wrapGap || 0; }
  return f;
}

function por(pai, filho, h, v) {
  pai.appendChild(filho);
  if (h) filho.layoutSizingHorizontal = h;
  if (v) filho.layoutSizingVertical = v;
  if (filho.type === 'TEXT') {
    if (h === 'FILL') filho.textAutoResize = 'HEIGHT';
    recortar(filho);
  }
  return filho;
}

function fixo(f, w, h) {
  f.primaryAxisSizingMode = 'FIXED';
  f.counterAxisSizingMode = 'FIXED';
  f.resize(w, h);
}

function espaco(pai, h) {
  const e = figma.createFrame();
  e.name = 'Espaço ' + h;
  e.resize(1, h);
  e.fills = [];
  pai.appendChild(e);
  return e;
}

function setProp(inst, nome, valor) {
  const props = inst.componentProperties || {};
  for (const k of Object.keys(props)) {
    if (k.split('#')[0] === nome && props[k].type !== 'VARIANT') {
      const o = {}; o[k] = valor; inst.setProperties(o); return true;
    }
  }
  return false;
}
function setTxt(no, nome, valor) {
  if (no.type === 'INSTANCE') { try { if (setProp(no, nome, valor)) return; } catch (e) { /* direto */ } }
  const t = no.findOne((n) => n.type === 'TEXT' && n.name === nome);
  if (t) t.characters = valor;
}
function acharNo(no, nome) { return no.findOne((n) => n.name === nome); }
function prop(comp, nome, tipo, padrao, no, campo) {
  try {
    const k = comp.addComponentProperty(nome, tipo, padrao);
    const refs = {}; refs[campo || (tipo === 'TEXT' ? 'characters' : 'visible')] = k;
    no.componentPropertyReferences = refs;
    return k;
  } catch (e) { return null; }
}
function arrumarSet(set, dir) {
  try {
    set.layoutMode = dir || 'HORIZONTAL';
    set.primaryAxisSizingMode = 'AUTO'; set.counterAxisSizingMode = 'AUTO';
    set.itemSpacing = 24;
    set.paddingTop = set.paddingBottom = set.paddingLeft = set.paddingRight = 24;
    set.counterAxisAlignItems = 'MIN';
    set.strokes = [SOLIDO('#9747FF')]; set.strokeWeight = 1; set.dashPattern = [10, 5]; set.cornerRadius = 8;
  } catch (e) { /* estético */ }
}

// ---------------------------------------------------------------- ícones
const ICONES_SVG = {
  inicio: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
  lista: 'M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z',
  config: 'M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z',
  hospital: 'M19 3H5c-1.1 0-1.99.9-1.99 2L3 19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-1 11h-4v4h-4v-4H6v-4h4V6h4v4h4v4z',
  triagem: 'M10.5 13H8v-3h2.5V7.5h3V10H16v3h-2.5v2.5h-3V13zM12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3z',
  local: 'M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z',
  guia: 'M22 7h-9v2h9V7zm0 8h-9v2h9v-2zM5.54 11L2 7.46l1.41-1.41 2.12 2.12 4.24-4.24 1.41 1.41L5.54 11zm0 8L2 15.46l1.41-1.41 2.12 2.12 4.24-4.24 1.41 1.41L5.54 19z',
  ideia: 'M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z',
  chevron: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  telefone: 'M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z',
  busca: 'M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z',
  cadeado: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
  check: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  aviso: 'M12 5.99L19.53 19H4.47L12 5.99M12 2L1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z',
  notificacao: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  temaEscuro: 'M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z',
  sair: 'M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z',
  olho: 'M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z',
  seta: 'M7 10l5 5 5-5z',
  wifi: 'M1 9l2 2c4.97-4.97 13.03-4.97 18 0l2-2C16.93 2.93 7.08 2.93 1 9zm8 8l3 3 3-3c-1.65-1.66-4.34-1.66-6 0zm-4-4l2 2c2.76-2.76 7.24-2.76 10 0l2-2C15.14 9.14 8.87 9.14 5 13z',
  sinal: 'M2 22h20V2z',
  bateria: 'M15.67 4H14V2h-4v2H8.33C7.6 4 7 4.6 7 5.33v15.33C7 21.4 7.6 22 8.33 22h7.33c.74 0 1.34-.6 1.34-1.33V5.33C17 4.6 16.4 4 15.67 4z',
  voltar: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  estoque: 'M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zm-5 12H9v-2h6v2zm5-7H4V4l16-.02V7z',
  historico: 'M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z',
  perfil: 'M21.9 8.89l-1.05-4.37c-.22-.9-1-1.52-1.91-1.52H5.05c-.9 0-1.69.63-1.9 1.52L2.1 8.89c-.24 1.02-.02 2.06.62 2.88.08.11.19.19.28.29V19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-6.94c.09-.09.2-.18.28-.28.64-.82.87-1.87.62-2.89zM5 19v-6.03c.08.01.15.03.23.03.87 0 1.66-.36 2.24-.95.6.6 1.4.95 2.31.95.87 0 1.65-.36 2.23-.93.59.57 1.39.93 2.29.93.84 0 1.64-.35 2.24-.95.58.59 1.37.95 2.24.95.08 0 .15-.02.23-.03V19H5z',
  plano: 'M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z',
};
const NOMES_ICONES = {
  inicio: 'Home', lista: 'List', config: 'Settings', hospital: 'LocalHospital', triagem: 'HealthAndSafety',
  local: 'LocationOn', guia: 'Checklist', ideia: 'Lightbulb', chevron: 'ChevronRight', telefone: 'Phone',
  busca: 'Search', cadeado: 'Lock', check: 'CheckCircle', aviso: 'WarningAmber', notificacao: 'Notifications',
  temaEscuro: 'DarkMode', sair: 'Logout', olho: 'Visibility', seta: 'ArrowDropDown', wifi: 'Wifi',
  sinal: 'SignalCellular', bateria: 'Battery', voltar: 'ArrowBack', estoque: 'Inventory', historico: 'History',
  perfil: 'Storefront', plano: 'Star',
};
const IC = {};
function pintarVetores(no, v, op) {
  for (const x of no.findAll((n) => n.type === 'VECTOR' || n.type === 'BOOLEAN_OPERATION')) x.fills = [tinta(v, op)];
}
async function criarIcones(pai) {
  for (const nome of Object.keys(ICONES_SVG)) {
    const cel = bloco(pai, 'Célula ' + nome, { dir: 'V', gap: 8, cross: 'CENTER', pad: 12, radius: 8, fill: SOLIDO('#FFF5F5') });
    const n = figma.createNodeFromSvg('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="' + ICONES_SVG[nome] + '" fill="#000000"/></svg>');
    const c = figma.createComponent();
    c.name = 'Ícone/' + NOMES_ICONES[nome];
    c.resize(24, 24); c.fills = []; c.clipsContent = false;
    for (const ch of n.children.slice()) {
      c.appendChild(ch); ch.name = 'Vetor';
      try { ch.constraints = { horizontal: 'SCALE', vertical: 'SCALE' }; } catch (e) { /* ok */ }
    }
    n.remove();
    pintarVetores(c, V['on-surface']);
    cel.appendChild(c);
    por(cel, await textoDoc(NOMES_ICONES[nome], 10, 'regular', '#8A6B6F'));
    IC[nome] = c;
  }
}
function icone(nome, cor, tam) {
  const i = IC[nome].createInstance();
  if (cor) pintarVetores(i, cor);
  if (tam && tam !== 24) i.resize(tam, tam);
  i.name = 'Ícone ' + NOMES_ICONES[nome];
  return i;
}

function gotaPath(cx, cy, s) {
  const h = s / 2, x0 = cx - h, y0 = cy - h;
  return 'M' + x0 + ' ' + (y0 + s) + ' L' + x0 + ' ' + (y0 + h) +
    ' A' + h + ' ' + h + ' 0 0 1 ' + (x0 + h) + ' ' + y0 +
    ' A' + h + ' ' + h + ' 0 0 1 ' + (x0 + s) + ' ' + (y0 + h) +
    ' A' + h + ' ' + h + ' 0 0 1 ' + (x0 + h) + ' ' + (y0 + s) + ' Z';
}
function svgGotas(tam, gotas, rot) {
  let corpo = '';
  for (const g of gotas) corpo += '<path d="' + gotaPath(g[0], g[1], g[2]) + '" transform="rotate(' + rot + ' ' + g[0] + ' ' + g[1] + ')" fill="#FFFFFF" fill-opacity="' + g[3] + '"/>';
  const n = figma.createNodeFromSvg('<svg xmlns="http://www.w3.org/2000/svg" width="' + tam + '" height="' + tam + '" viewBox="0 0 ' + tam + ' ' + tam + '">' + corpo + '</svg>');
  n.fills = []; n.clipsContent = false;
  return n;
}

// ---------------------------------------------------------------- componentes
const SB = {};
let ITEM, STAT;
const HDR = {};
const BTN = {}, CAMPO = {}, NAV_D = {}, NAV_H = {}, CHIP = {}, OPC = {}, SELO = {}, SW = {};

async function celula(pai, titulo, desc) {
  const c = bloco(pai, titulo, { dir: 'V', gap: 12 });
  por(c, await textoDoc(titulo, 16, 'bold'));
  if (desc) por(c, await textoDoc(desc, 12, 'regular', '#8A6B6F'));
  return c;
}
function novoComp(pai, nome, dir) {
  const c = figma.createComponent();
  pai.appendChild(c);
  c.name = nome; c.fills = []; c.clipsContent = false;
  c.layoutMode = dir === 'V' ? 'VERTICAL' : 'HORIZONTAL';
  c.primaryAxisSizingMode = 'AUTO'; c.counterAxisSizingMode = 'AUTO';
  return c;
}
function padC(c, t, r, b, l) { c.paddingTop = t; c.paddingRight = r; c.paddingBottom = b; c.paddingLeft = l; }

async function criarStatusBar(pai) {
  const cel = await celula(pai, 'Barra de status', 'Barra de status do Android (54 dp, transparente — o app desenha por baixo).');
  const c = novoComp(cel, 'Barra de status', 'H');
  fixo(c, W, SB_H);
  padC(c, 0, 24, 0, 24);
  c.primaryAxisAlignItems = 'SPACE_BETWEEN'; c.counterAxisAlignItems = 'CENTER';
  por(c, await texto('9:30', 'labelLarge', V['on-background'], { nome: 'Hora' }));
  const ic = bloco(c, 'Ícones', { dir: 'H', gap: 6, cross: 'CENTER' });
  for (const n of ['sinal', 'wifi', 'bateria']) por(ic, icone(n, V['on-background'], 18));
  SB.c = c;
}

async function criarCabecalho(pai) {
  const cel = await celula(pai, 'Cabeçalho vermelho', 'CabecalhoVermelho.kt — fica logo abaixo da barra de status. Simples: 20 + título 22sp + 20 = 66 dp. Com voltar (sub-telas): 8 + botão 48 + 8 = 64 dp. Rola junto com o conteúdo.');
  const comps = [];
  for (const voltar of ['Não', 'Sim']) {
    const c = novoComp(cel, 'Voltar=' + voltar, 'H');
    c.primaryAxisSizingMode = 'FIXED';
    c.resize(W, 64);
    c.counterAxisSizingMode = 'AUTO';
    c.counterAxisAlignItems = 'CENTER';
    c.fills = [tinta(V.vermelho)];
    if (voltar === 'Sim') {
      padC(c, 8, 8, 8, 8);
      c.itemSpacing = 4;
      const b = bloco(c, 'Voltar', { dir: 'H', main: 'CENTER', cross: 'CENTER', radius: 24 });
      fixo(b, 48, 48);
      por(b, icone('voltar', V.branco));
    } else {
      padC(c, 20, 24, 20, 24);
    }
    const t = await texto('Título', 'cabecalho', V.branco, { nome: 'Título' });
    por(c, t, 'FILL');
    prop(c, 'Título', 'TEXT', 'Título', t);
    HDR[voltar] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Cabeçalho vermelho';
  arrumarSet(set, 'VERTICAL');
}

// Pílula visível de 40 dp; nas telas cada botão fica numa moldura de toque de 48 dp.
function pilula(pai, nome, o) {
  const c = novoComp(pai, nome, 'H');
  c.counterAxisSizingMode = 'FIXED';
  c.resize(120, 40);
  c.primaryAxisSizingMode = 'AUTO';
  padC(c, 0, o.padH, 0, o.padH);
  c.itemSpacing = o.gap || 0;
  c.primaryAxisAlignItems = 'CENTER';
  c.counterAxisAlignItems = 'CENTER';
  c.cornerRadius = o.raio;
  if (o.fill) c.fills = [o.fill];
  if (o.stroke) { c.strokes = [o.stroke]; c.strokeWeight = 1; c.strokeAlign = 'INSIDE'; }
  return c;
}

async function criarBotoes(pai) {
  const cel = await celula(pai, 'Botões', 'Pílula de 40 dp (área de toque de 48 dp nas telas). Primário: raio 14. Contorno (M3 novo): borda outline-variant e conteúdo on-surface-variant. Texto: vermelho.');
  const linha = bloco(cel, 'Botões', { dir: 'H', gap: 24, cross: 'CENTER' });
  const a = pilula(linha, 'Botão/Primário', { padH: 24, raio: 14, fill: tinta(V.vermelho) });
  const tp = await texto('Entrar', 'labelLarge', V.branco, { nome: 'Rótulo' });
  por(a, tp);
  prop(a, 'Rótulo', 'TEXT', 'Entrar', tp);
  BTN.primario = a;
  const b = pilula(linha, 'Botão/Contorno', { padH: 24, raio: 20, gap: 6, stroke: tinta(V['outline-variant']) });
  const io = icone('sair', V['on-surface-variant']);
  io.name = 'Ícone';
  por(b, io);
  io.visible = false;
  const to = await texto('Sair', 'labelLarge', V['on-surface-variant'], { nome: 'Rótulo' });
  por(b, to);
  prop(b, 'Rótulo', 'TEXT', 'Sair', to);
  prop(b, 'Mostrar ícone', 'BOOLEAN', false, io);
  BTN.contorno = b;
  const d = pilula(linha, 'Botão/Texto', { padH: 12, raio: 20 });
  const tt = await texto('Já tem conta? Entrar', 'labelLarge', V.primary, { nome: 'Rótulo' });
  por(d, tt);
  prop(d, 'Rótulo', 'TEXT', 'Já tem conta? Entrar', tt);
  BTN.texto = d;
}

async function criarCampo(pai) {
  const cel = await celula(pai, 'Campo de texto', 'OutlinedTextField. Com rótulo ocupa 64 dp (8 para o rótulo flutuante + 56); sem rótulo, 56 dp.');
  const comps = [];
  for (const estado of ['Vazio', 'Preenchido', 'Sem rótulo']) {
    const c = novoComp(cel, 'Estado=' + estado, 'V');
    c.counterAxisSizingMode = 'FIXED';
    c.resize(363, 64);
    padC(c, estado === 'Sem rótulo' ? 0 : 8, 0, 0, 0);
    const box = bloco(c, 'Caixa', { dir: 'H', gap: 12, pad: [0, 12, 0, 16], cross: 'CENTER', radius: 4, stroke: tinta(V.outline) }, 'FILL');
    box.counterAxisSizingMode = 'FIXED';
    box.resize(363, 56);
    box.layoutSizingHorizontal = 'FILL';
    box.clipsContent = false;
    const ini = icone('busca', V['on-surface-variant']);
    ini.name = 'Ícone inicial';
    por(box, ini);
    ini.visible = false;
    por(box, await texto(estado === 'Preenchido' ? 'Valor' : 'Rótulo', 'bodyLarge', estado === 'Preenchido' ? V['on-surface'] : V['on-surface-variant'], { nome: 'Texto' }), 'FILL');
    const fim = icone('olho', V['on-surface-variant']);
    fim.name = 'Ícone final';
    por(box, fim);
    fim.visible = false;
    if (estado === 'Preenchido') {
      const lab = caixa('Rótulo flutuante', { dir: 'H', pad: [0, 4, 0, 4], fill: tinta(V.background) });
      box.appendChild(lab);
      lab.layoutPositioning = 'ABSOLUTE';
      por(lab, await texto('Rótulo', 'bodySmall', V['on-surface-variant'], { nome: 'Rótulo' }));
      lab.x = 12; lab.y = -8;
    }
    CAMPO[estado] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Campo de texto';
  arrumarSet(set, 'VERTICAL');
}

async function criarNav(pai, nomeSet, abas, destino) {
  const cel = await celula(pai, nomeSet, 'NavigationBar (80 dp) + área de gestos do Android (24 dp).');
  const comps = [];
  for (const sel of abas) {
    const c = novoComp(cel, 'Aba=' + sel.rotulo, 'V');
    fixo(c, W, 104);
    c.fills = [tinta(V.surface)];
    const linha = bloco(c, 'Itens', { dir: 'H' }, 'FILL');
    fixo(linha, W, 80);
    for (const a of abas) {
      const cor = a === sel ? V.primary : V['on-surface-variant'];
      const it = bloco(linha, 'Aba ' + a.rotulo, { dir: 'V', gap: 8, pad: [16, 0, 16, 0], cross: 'CENTER' }, 'FILL', 'FILL');
      por(it, icone(a.icone, cor));
      por(it, await texto(a.rotulo, 'labelMedium', cor, { nome: 'Rótulo' }));
    }
    const gesto = bloco(c, 'Área de gestos', { dir: 'H', main: 'CENTER', cross: 'CENTER' }, 'FILL');
    fixo(gesto, W, 24);
    const pil = figma.createRectangle();
    pil.name = 'Barra de gesto';
    pil.resize(108, 4);
    pil.cornerRadius = 2;
    pil.fills = [tinta(V['on-surface'])];
    gesto.appendChild(pil);
    destino[sel.id] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = nomeSet;
  arrumarSet(set, 'VERTICAL');
}

async function criarItemLista(pai) {
  const cel = await celula(pai, 'Item da lista', 'Card da aba Lista: 371×74, raio 16, padding 14.');
  const c = novoComp(cel, 'Item da lista', 'H');
  c.primaryAxisSizingMode = 'FIXED';
  c.resize(371, 74);
  c.counterAxisSizingMode = 'AUTO';
  padC(c, 14, 14, 14, 14);
  c.counterAxisAlignItems = 'CENTER';
  c.cornerRadius = 16;
  c.fills = [tinta(V.surface)];
  sombra(c);
  const esq = bloco(c, 'Conteúdo', { dir: 'H', cross: 'CENTER', gap: 14 }, 'FILL');
  const box = bloco(esq, 'Ícone fundo', { dir: 'H', main: 'CENTER', cross: 'CENTER', radius: 12, fill: tinta(V['rosa-claro']) });
  fixo(box, 44, 44);
  const ic = icone('triagem', V.vermelho);
  ic.name = 'Ícone';
  por(box, ic);
  const col = bloco(esq, 'Textos', { dir: 'V', gap: 2 }, 'FILL');
  const t1 = await texto('Posso doar?', 'titleMedium', V['on-surface'], { nome: 'Título' });
  por(col, t1, 'FILL');
  const t2 = await texto('Faça a triagem rápida', 'bodyMedium', V['on-surface-variant'], { nome: 'Descrição' });
  por(col, t2, 'FILL');
  por(c, icone('chevron', V['on-surface-variant']));
  prop(c, 'Título', 'TEXT', 'Posso doar?', t1);
  prop(c, 'Descrição', 'TEXT', 'Faça a triagem rápida', t2);
  prop(c, 'Ícone', 'INSTANCE_SWAP', IC.triagem.id, ic, 'mainComponent');
  ITEM = c;
}

async function criarEstatistica(pai) {
  const cel = await celula(pai, 'Card de estatística', 'EstatisticaCard — branco 15% sobre o vermelho. 114×106.');
  const fundo = bloco(cel, 'Fundo demonstração', { dir: 'H', pad: 16, radius: 12, fill: tinta(V.vermelho) });
  const c = novoComp(fundo, 'Card de estatística', 'V');
  padC(c, 14, 4, 14, 4);
  c.counterAxisSizingMode = 'FIXED';
  c.resize(114, 106);
  c.primaryAxisSizingMode = 'AUTO';
  c.counterAxisAlignItems = 'CENTER';
  c.cornerRadius = 14;
  c.fills = [SOLIDO('#FFFFFF', 0.15)];
  const e = await texto('🩸', 'padraoEmoji20', null, { nome: 'Emoji', align: 'CENTER' });
  por(c, e);
  espaco(c, 6);
  const v = await texto('4 vidas', 'padrao15b', V.branco, { nome: 'Valor', align: 'CENTER' });
  por(c, v, 'FILL');
  const l = await texto('por doação', 'padrao10', V['rosa-claro'], { nome: 'Legenda', align: 'CENTER' });
  por(c, l, 'FILL');
  prop(c, 'Emoji', 'TEXT', '🩸', e);
  prop(c, 'Valor', 'TEXT', '4 vidas', v);
  prop(c, 'Legenda', 'TEXT', 'por doação', l);
  STAT = c;
}

async function criarChips(pai) {
  const cel = await celula(pai, 'Chip de nível', 'ChipNivel — 32 dp visíveis numa área de toque de 48 dp.');
  const comps = [];
  for (const sel of ['Sim', 'Não']) {
    const c = novoComp(cel, 'Selecionado=' + sel, 'H');
    padC(c, 8, 10, 8, 10);
    c.cornerRadius = 10;
    c.fills = [tinta(sel === 'Sim' ? V.vermelho : V['surface-variant'])];
    por(c, await texto('🟢 Estável', 'labelMediumBold', sel === 'Sim' ? V.branco : V['on-surface-variant'], { nome: 'Rótulo' }));
    CHIP[sel] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Chip de nível';
  arrumarSet(set);
}

async function criarOpcoes(pai) {
  const cel = await celula(pai, 'Opção Sim/Não', 'OpcaoResposta — 96×40 visíveis, área de toque 48 dp.');
  const comps = [];
  for (const sel of ['Sim', 'Não']) {
    const c = novoComp(cel, 'Selecionado=' + sel, 'H');
    fixo(c, 96, 40);
    c.primaryAxisAlignItems = 'CENTER';
    c.counterAxisAlignItems = 'CENTER';
    c.cornerRadius = 12;
    c.fills = [tinta(sel === 'Sim' ? V.vermelho : V['surface-variant'])];
    por(c, await texto('Sim', 'labelLargeBold', sel === 'Sim' ? V.branco : V['on-surface-variant'], { nome: 'Rótulo', align: 'CENTER' }));
    OPC[sel] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Opção Sim/Não';
  arrumarSet(set);
}

async function criarSelos(pai) {
  const cel = await celula(pai, 'Selo de veredito', 'Selo da tela Mitos e verdades (cor da marca a 15%).');
  const dados = [['Mito', '❌ Mito', V.vermelho], ['Verdade', '✅ Verdade', V.verde], ['Depende', '⚠️ Depende', V['amarelo-alerta']]];
  const comps = [];
  for (const [tipo, rot, cor] of dados) {
    const c = novoComp(cel, 'Tipo=' + tipo, 'H');
    padC(c, 6, 12, 6, 12);
    c.cornerRadius = 20;
    c.fills = [tinta(cor, 0.15)];
    por(c, await texto(rot, 'labelLarge', cor, { nome: 'Rótulo' }));
    SELO[tipo] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Selo de veredito';
  arrumarSet(set);
}

async function criarSwitch(pai) {
  const cel = await celula(pai, 'Switch', 'Switch do Material 3 — trilho 52×32 numa área de toque de 48 dp.');
  const comps = [];
  for (const on of ['Sim', 'Não']) {
    const c = figma.createComponent();
    cel.appendChild(c);
    c.name = 'Ligado=' + on;
    c.resize(52, 48);
    c.fills = []; c.clipsContent = false;
    const trilho = figma.createRectangle();
    c.appendChild(trilho);
    trilho.name = 'Trilho'; trilho.resize(52, 32); trilho.y = 8; trilho.cornerRadius = 16;
    const bola = figma.createEllipse();
    c.appendChild(bola);
    bola.name = 'Polegar';
    if (on === 'Sim') {
      trilho.fills = [tinta(V.primary)];
      bola.resize(24, 24); bola.x = 24; bola.y = 12;
      bola.fills = [tinta(V['on-primary'])];
    } else {
      trilho.fills = [tinta(V['switch-off'])];
      trilho.strokes = [tinta(V.outline)]; trilho.strokeWeight = 2; trilho.strokeAlign = 'INSIDE';
      bola.resize(16, 16); bola.x = 8; bola.y = 16;
      bola.fills = [tinta(V.outline)];
    }
    SW[on] = c;
    comps.push(c);
  }
  const set = figma.combineAsVariants(comps, cel);
  set.name = 'Switch';
  arrumarSet(set);
}

// ---------------------------------------------------------------- instâncias
function cabecalho(pai, titulo, ctx, voltarPara) {
  const h = HDR[voltarPara ? 'Sim' : 'Não'].createInstance();
  por(pai, h, 'FILL');
  setTxt(h, 'Título', titulo);
  h.name = 'Cabeçalho · ' + titulo;
  if (voltarPara) {
    const b = acharNo(h, 'Voltar');
    if (b) ctx.links.push([b, voltarPara]);
  }
  return h;
}
function botao(rotulo, o) {
  o = o || {};
  const b = BTN.primario.createInstance();
  setTxt(b, 'Rótulo', rotulo);
  b.name = 'Botão · ' + rotulo;
  if (o.cor) b.fills = [tinta(o.cor)];
  if (o.raio != null) b.cornerRadius = o.raio;
  return b;
}
function botaoContorno(rotulo, o) {
  o = o || {};
  const b = BTN.contorno.createInstance();
  setTxt(b, 'Rótulo', rotulo);
  b.name = 'Botão contorno · ' + rotulo;
  if (o.icone) {
    let feito = false;
    try { feito = setProp(b, 'Mostrar ícone', true); } catch (e) { /* direto */ }
    if (!feito) acharNo(b, 'Ícone').visible = true;
  }
  if (o.raio != null) b.cornerRadius = o.raio;
  return b;
}
function botaoTexto(rotulo) {
  const b = BTN.texto.createInstance();
  setTxt(b, 'Rótulo', rotulo);
  b.name = 'Botão texto · ' + rotulo;
  return b;
}
function campo(rotulo, o) {
  o = o || {};
  const estado = o.semRotulo ? 'Sem rótulo' : (o.valor != null ? 'Preenchido' : 'Vazio');
  const c = CAMPO[estado].createInstance();
  c.name = 'Campo · ' + rotulo;
  if (estado === 'Preenchido') { setTxt(c, 'Texto', o.valor); setTxt(c, 'Rótulo', rotulo); }
  else setTxt(c, 'Texto', rotulo);
  if (o.fim) {
    const n = acharNo(c, 'Ícone final');
    n.visible = true;
    if (o.fim !== 'olho') n.swapComponent(IC[o.fim]);
    pintarVetores(n, V['on-surface-variant']);
  }
  if (o.inicio) {
    const n = acharNo(c, 'Ícone inicial');
    n.visible = true;
    pintarVetores(n, V['on-surface-variant']);
  }
  if (o.fundo) { const l = acharNo(c, 'Rótulo flutuante'); if (l) l.fills = [tinta(o.fundo)]; }
  if (o.corTexto) acharNo(c, 'Texto').fills = [tinta(o.corTexto)];
  return c;
}
function chip(rotulo, sel) {
  const i = (sel ? CHIP.Sim : CHIP['Não']).createInstance();
  setTxt(i, 'Rótulo', rotulo);
  i.name = 'Chip · ' + rotulo;
  return i;
}
function opcao(rotulo, sel) {
  const i = (sel ? OPC.Sim : OPC['Não']).createInstance();
  setTxt(i, 'Rótulo', rotulo);
  i.name = 'Opção · ' + rotulo;
  return i;
}
// Moldura da área de toque (48 dp): padding vertical em volta do componente.
function toque(pai, inst, h, pv) {
  const v = pv == null ? 4 : pv;
  const w = bloco(pai, 'Toque · ' + inst.name, { dir: 'V', pad: [v, 0, v, 0] }, h);
  por(w, inst, h);
  return inst;
}
function cartao(pai, nome, o) {
  o = o || {};
  const fill = o.translucido ? SOLIDO(o.translucido[0], o.translucido[1]) : tinta(o.fundo || V.surface);
  const c = bloco(pai, nome, { dir: o.dir || 'V', pad: o.pad != null ? o.pad : 16, radius: o.raio || 16, fill: fill, gap: o.gap || 0,
    main: o.main, cross: o.cross }, 'FILL');
  if (!o.translucido && !o.semSombra) sombra(c);
  return c;
}

// ---------------------------------------------------------------- tela
// Estrutura igual ao app: barra de status (54) transparente por cima,
// área de rolagem e, nas telas com Scaffold, barra de navegação fixa embaixo.
const ABAS_DOADOR = [
  { id: 'inicio', rotulo: 'Início', icone: 'inicio' },
  { id: 'lista', rotulo: 'Lista', icone: 'lista' },
  { id: 'config', rotulo: 'Configuração', icone: 'config' },
];
const ABAS_HOSPITAL = [
  { id: 'painel', rotulo: 'Hospital', icone: 'hospital' },
  { id: 'configHosp', rotulo: 'Configuração', icone: 'config' },
];

function tela(ctx, nome, o) {
  o = o || {};
  const f = figma.createFrame();
  f.name = nome;
  f.resize(W, H);
  f.fills = [tinta(V.background)];
  f.clipsContent = true;
  const comScaffold = !!o.nav;
  const rol = figma.createFrame();
  f.appendChild(rol);
  rol.name = 'Rolagem';
  rol.x = 0; rol.y = comScaffold ? SB_H : 0;
  rol.resize(W, comScaffold ? NAV_Y - SB_H : H);
  rol.layoutMode = 'VERTICAL';
  rol.primaryAxisSizingMode = 'FIXED'; rol.counterAxisSizingMode = 'FIXED';
  rol.fills = o.fundoRolagem ? [tinta(o.fundoRolagem)] : [];
  rol.clipsContent = true;
  rol.overflowDirection = 'VERTICAL';
  rol.itemSpacing = o.gap || 0;
  if (o.pad) { rol.paddingTop = o.pad[0]; rol.paddingRight = o.pad[1]; rol.paddingBottom = o.pad[2]; rol.paddingLeft = o.pad[3]; }
  if (o.centro) rol.primaryAxisAlignItems = 'CENTER';
  if (o.cross) rol.counterAxisAlignItems = o.cross;
  const sb = SB.c.createInstance();
  f.appendChild(sb);
  sb.x = 0; sb.y = 0; sb.name = 'Barra de status';
  if (comScaffold) {
    const abas = o.nav === 'doador' ? ABAS_DOADOR : ABAS_HOSPITAL;
    const n = (o.nav === 'doador' ? NAV_D : NAV_H)[o.aba].createInstance();
    f.appendChild(n);
    n.x = 0; n.y = NAV_Y; n.name = 'Barra de navegação';
    for (const a of abas) {
      if (a.id === o.aba) continue;
      const item = acharNo(n, 'Aba ' + a.rotulo);
      if (item) ctx.links.push([item, a.id]);
    }
  }
  return { f, rol };
}

// Conteúdo das LazyColumn: cabeçalho de largura total + itens com padding 20.
function lista(rol, titulo, gap, fundo, ctx, voltarPara) {
  rol.itemSpacing = gap;
  cabecalho(rol, titulo, ctx, voltarPara);
  return bloco(rol, 'Conteúdo', { dir: 'V', gap: gap, pad: [0, 20, fundo, 20] }, 'FILL');
}

// ---------------------------------------------------------------- telas
async function cabecalhoAuth(col, emoji, titulo, sub) {
  por(col, await texto(emoji, 'emoji48', null, { nome: 'Emoji', align: 'CENTER' }));
  espaco(col, 12);
  por(col, await texto(titulo, 'headline', V['on-background']));
  espaco(col, 4);
  por(col, await texto(sub, 'bodyMedium', V['on-surface-variant'], { align: 'CENTER' }), 'FILL');
}

async function linkLongo(pai, rotulo) {
  const b = bloco(pai, 'Botão texto · ' + rotulo.slice(0, 30), { dir: 'V', pad: [8, 12, 8, 12], radius: 20 }, 'FILL');
  por(b, await texto(rotulo, 'labelLarge', V.primary, { nome: 'Rótulo' }), 'FILL');
  return b;
}

async function telaLogin(ctx) {
  const { f, rol } = tela(ctx, 'Login', { centro: true, cross: 'CENTER', pad: [24, 24, 24, 24] });
  await cabecalhoAuth(rol, '🩸', 'Entrar no Hemare', 'Bem-vindo(a) de volta! Acesse sua conta.');
  espaco(rol, 24);
  por(rol, campo('Email'), 'FILL');
  espaco(rol, 12);
  por(rol, campo('Senha', { fim: 'olho' }), 'FILL');
  espaco(rol, 20);
  ctx.links.push([toque(rol, botao('Entrar'), 'FILL'), 'inicio']);
  espaco(rol, 20);
  ctx.links.push([toque(rol, botaoTexto('Não tem conta? Cadastre-se')), 'cadastroDoador']);
  ctx.links.push([await linkLongo(rol, 'É um hospital ou hemocentro? Cadastre sua instituição'), 'cadastroHospital']);
  return f;
}

async function telaCadastroDoador(ctx) {
  const { f, rol } = tela(ctx, 'Cadastro — Doador', { centro: true, cross: 'CENTER', pad: [24, 24, 24, 24] });
  await cabecalhoAuth(rol, '🩸', 'Criar sua conta', 'Junte-se ao Hemare e ajude a salvar vidas.');
  espaco(rol, 24);
  por(rol, campo('Nome completo'), 'FILL');
  espaco(rol, 12);
  por(rol, campo('Email'), 'FILL');
  espaco(rol, 12);
  por(rol, campo('Senha (mín. 8 caracteres)', { fim: 'olho' }), 'FILL');
  espaco(rol, 20);
  ctx.links.push([toque(rol, botao('Cadastrar'), 'FILL'), 'inicio']);
  espaco(rol, 20);
  ctx.links.push([toque(rol, botaoTexto('Já tem conta? Entrar')), 'BACK']);
  return f;
}

async function telaCadastroHospital(ctx) {
  const { f, rol } = tela(ctx, 'Cadastro — Hospital', { gap: 14, pad: [24, 24, 24, 24] });
  const topo = bloco(rol, 'Topo', { dir: 'V', cross: 'CENTER' }, 'FILL');
  await cabecalhoAuth(topo, '🏥', 'Cadastrar hospital', 'Cadastre sua instituição e encontre doadores.');
  for (const c of ['Nome do hospital', 'CNPJ', 'CNES', 'Email institucional', 'Senha (mín. 8 caracteres)', 'CEP',
    'Endereço (rua)', 'Número', 'Bairro', 'Complemento (opcional)', 'Cidade']) {
    por(rol, campo(c, c.indexOf('Senha') === 0 ? { fim: 'olho' } : {}), 'FILL');
  }
  por(rol, campo('Estado (UF)', { fim: 'seta' }), 'FILL');
  ctx.links.push([toque(rol, botao('Cadastrar hospital'), 'FILL'), 'painel']);
  const rod = bloco(rol, 'Rodapé', { dir: 'V', cross: 'CENTER' }, 'FILL');
  ctx.links.push([toque(rod, botaoTexto('Voltar para o login')), 'BACK']);
  return f;
}

async function telaInicio(ctx) {
  const { f, rol } = tela(ctx, 'Início', { nav: 'doador', aba: 'inicio', fundoRolagem: V.vermelho, pad: [24, 24, 24, 24] });
  por(rol, await texto('Hemare', 'padrao20', V.branco));
  espaco(rol, 40);
  const t = por(rol, await texto('Sua doação pode salvar até 4 vidas', 'destaque', V.branco), 'FILL');
  t.textAutoResize = 'NONE';
  t.resize(t.width, 57);
  espaco(rol, 20);
  por(rol, await texto('Uma única doação pode salvar até quatro vidas. Doe sangue, doe esperança.', 'padrao16', V['rosa-claro']), 'FILL');
  espaco(rol, 32);
  const linha = bloco(rol, 'Estatísticas', { dir: 'H', gap: 10 }, 'FILL');
  for (const [e, v, l] of [['🩸', '4 vidas', 'por doação'], ['⏱️', '10 min', 'de doação'], ['❤️', '1,6%', 'da população doa']]) {
    const s = STAT.createInstance();
    por(linha, s, 'FILL');
    setTxt(s, 'Emoji', e); setTxt(s, 'Valor', v); setTxt(s, 'Legenda', l);
    s.name = 'Estatística · ' + v;
  }
  const ilu = bloco(rol, 'Ilustração', { dir: 'H', main: 'CENTER', pad: [24, 0, 0, 0] }, 'FILL');
  const caixaG = bloco(ilu, 'Gotas', { dir: 'NONE' });
  caixaG.resize(220, 220);
  const g = svgGotas(240, [[120, 120, 160, 0.10], [60, 160, 90, 0.18], [175, 70, 60, 0.22]], 45);
  g.name = 'Ilustração gotas';
  caixaG.appendChild(g);
  g.x = -10; g.y = -10;
  return f;
}

async function telaLista(ctx) {
  const { f, rol } = tela(ctx, 'Lista', { nav: 'doador', aba: 'lista' });
  const col = lista(rol, 'Aprenda sobre doação', 14, 20);
  for (const [dest, ic, t, d] of [
    ['possoDoar', 'triagem', 'Posso doar?', 'Faça a triagem rápida'],
    ['ondeDoar', 'local', 'Onde doar', 'Hemocentros perto de você'],
    ['guia', 'guia', 'Guia de doação', 'Antes, durante e depois'],
    ['mitos', 'ideia', 'Mitos e verdades', 'Tire suas dúvidas']]) {
    const i = ITEM.createInstance();
    por(col, i, 'FILL');
    setTxt(i, 'Título', t); setTxt(i, 'Descrição', d);
    if (ic !== 'triagem') {
      let feito = false;
      try { feito = setProp(i, 'Ícone', IC[ic].id); } catch (e) { /* troca direta */ }
      if (!feito) acharNo(i, 'Ícone').swapComponent(IC[ic]);
    }
    pintarVetores(acharNo(i, 'Ícone'), V.vermelho);
    i.name = 'Item · ' + t;
    ctx.links.push([i, dest]);
  }
  return f;
}

const PERGUNTAS = [
  ['Situações recentes', [
    ['tatuagemRecente', 'Fez tatuagem ou micropigmentação nos últimos 12 meses?'],
    ['gripeResfriado', 'Está com gripe ou resfriado (ou teve há poucos dias)?'],
    ['bebidaAlcoolica', 'Ingeriu bebida alcoólica nas últimas 12 horas?'],
    ['gravidezOuPosParto', 'Está grávida ou teve parto recentemente?']]],
  ['Saúde', [
    ['temHIV', 'Você tem HIV/AIDS?'], ['temHepatiteB', 'Você tem Hepatite B?'], ['temHepatiteC', 'Você tem Hepatite C?'],
    ['temHTLV', 'Você tem HTLV?'], ['temChagas', 'Você tem Doença de Chagas?'],
    ['hepatiteAposOnzeAnos', 'Teve hepatite após os 11 anos de idade?'], ['usaDrogasInjetaveis', 'Faz uso de drogas injetáveis?']]],
  ['Condições a confirmar', [
    ['temDiabetes', 'Você tem diabetes?'], ['temHipertensao', 'Você tem hipertensão (pressão alta)?'],
    ['usaMedicacaoContinua', 'Você usa algum medicamento controlado ou de uso contínuo?']]],
];

async function campoNumero(pai, rotulo, valor, placeholder) {
  const c = bloco(pai, 'Campo · ' + rotulo, { dir: 'V', gap: 6 }, 'FILL');
  por(c, await texto(rotulo, 'bodyLarge', V['on-surface']));
  por(c, campo(valor || placeholder, valor ? { semRotulo: true, corTexto: V['on-surface'] } : { semRotulo: true }), 'FILL');
}

async function telaPossoDoar(ctx, comResultado) {
  const nome = comResultado ? 'Posso doar? — resultado' : 'Posso doar?';
  const { f, rol } = tela(ctx, nome, { nav: 'doador', aba: 'lista' });
  const col = lista(rol, 'Posso doar?', 14, 24, ctx, 'lista');
  por(col, await texto('Preencha suas informações para receber uma orientação sobre a doação. Isso não substitui a triagem clínica feita no hemocentro.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  const sobre = cartao(col, 'Sobre você');
  por(sobre, await texto('Sobre você', 'titleMedium', V['on-surface']), 'FILL');
  espaco(sobre, 12);
  await campoNumero(sobre, 'Sua idade', comResultado ? '25' : null, 'anos');
  espaco(sobre, 12);
  await campoNumero(sobre, 'Seu peso (kg)', comResultado ? '70' : null, 'kg');
  for (const [titulo, perguntas] of PERGUNTAS) {
    const g = cartao(col, titulo);
    por(g, await texto(titulo, 'titleMedium', V['on-surface']), 'FILL');
    for (let i = 0; i < perguntas.length; i++) {
      const [id, txt] = perguntas[i];
      espaco(g, 14);
      por(g, await texto(txt, 'bodyLarge', V['on-surface']), 'FILL');
      espaco(g, 8);
      const r = bloco(g, 'Sim / Não', { dir: 'H', gap: 10 });
      const sim = comResultado && id === 'tatuagemRecente';
      const nao = comResultado && id !== 'tatuagemRecente';
      toque(r, opcao('Sim', sim));
      toque(r, opcao('Não', nao));
      if (i < perguntas.length - 1) espaco(g, 4);
    }
  }
  const ver = toque(col, botao('Ver resultado'), 'FILL');
  if (!comResultado) ctx.links.push([ver, 'possoDoarResultado', true]);
  if (comResultado) {
    const res = cartao(col, 'Resultado · Amarelo', { pad: 20, translucido: ['#E0B000', 0.12] });
    por(res, icone('aviso', V['amarelo-alerta'], 36));
    espaco(res, 10);
    por(res, await texto('Atenção: confirme alguns pontos no hemocentro', 'titleLarge', V['amarelo-alerta']), 'FILL');
    espaco(res, 10);
    const m = bloco(res, 'Motivo', { dir: 'H', pad: [0, 0, 8, 0] }, 'FILL');
    por(m, await texto('•  ', 'bodyMediumBold', V['amarelo-alerta']));
    por(m, await texto('Tatuagem/micropigmentação nos últimos 12 meses (1 ano) pede um tempo de espera.', 'bodyMedium', V['on-surface']), 'FILL');
    const aviso = await texto('⚠️ Esta é uma orientação informativa, não substitui a triagem clínica. A avaliação final é feita por um profissional no dia da doação.', 'bodySmall', V['on-surface-variant']);
    espaco(res, 6);
    por(res, aviso, 'FILL');
    ctx.links.push([toque(col, botaoContorno('Refazer', { raio: 14 }), 'FILL'), 'possoDoar']);
  }
  return f;
}

// Primeiros hemocentros oficiais (os mesmos que o app traz no banco do aparelho).
const HEMOCENTROS = [
  ['HEMOAL', 'Maceió - AL', 'Av. Jorge de Lima, 58 - Trapiche da Barra', '(82) 3315-2107'],
  ['HEMOAM', 'Manaus - AM', 'Av. Constantino Nery, 4397 - Chapada', '(92) 3655-0100'],
  ['HEMOBA', 'Salvador - BA', 'Ladeira do Hospital Geral, s/n - Brotas', '(71) 3116-5652'],
  ['HEMOCE', 'Fortaleza - CE', 'Av. José Bastos, 3390 - Rodolfo Teófilo', '(85) 3101-2296'],
  ['Hemocentro de Brasília - FHB', 'Brasília - DF', 'SMHN Quadra 03, Conj. A, Bl. 3 - Asa Norte', '(61) 3327-4413'],
];

async function telaOndeDoar(ctx) {
  const { f, rol } = tela(ctx, 'Onde doar', { nav: 'doador', aba: 'lista' });
  const col = lista(rol, 'Onde doar', 12, 20, ctx, 'lista');
  const topo = bloco(col, 'Busca', { dir: 'V', gap: 12 }, 'FILL');
  por(topo, await texto('📍 Hemocentros cadastrados — funciona também sem internet.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  por(topo, campo('Buscar por cidade ou estado', { semRotulo: true, inicio: 'busca' }), 'FILL');
  for (const [nome, cidadeUf, endereco, telefone] of HEMOCENTROS) {
    const c = cartao(col, 'Hemocentro · ' + nome);
    por(c, await texto(nome, 'titleMedium', V['on-surface']), 'FILL');
    espaco(c, 2);
    por(c, await texto(cidadeUf, 'bodyMedium', V['on-surface-variant']), 'FILL');
    for (const [ic, t] of [['local', endereco], ['telefone', telefone]]) {
      const l = bloco(c, 'Info', { dir: 'H', gap: 6, cross: 'CENTER', pad: [6, 0, 0, 0] }, 'FILL');
      por(l, icone(ic, V.primary));
      por(l, await texto(t, 'bodyMedium', V['on-surface-variant']));
    }
  }
  return f;
}

const GUIA = [
  ['Antes de doar', [
    ['🍽️', 'Alimentação e preparo', ['Alimente-se bem — nunca vá em jejum. Faça uma refeição leve algumas horas antes.', 'Reforce o ferro nos dias anteriores — carnes magras, feijão, lentilha, ovos e folhas verde-escuras.', 'Hidrate-se — beba bastante água no dia anterior e antes de doar.', 'Descanse — durma bem na última noite.']],
    ['🚫', 'Evite no dia', ['Alimentos gordurosos, frituras e fast-food.', 'Bebida alcoólica nas 12 horas anteriores.', 'Excesso de café.']]]],
  ['No dia da doação', [
    ['⏱️', 'Como funciona', ['O processo completo (cadastro, triagem, coleta e lanche) leva em média 40 minutos.', 'A coleta em si dura só 5 a 15 minutos.', 'Leve um documento oficial com foto.']]]],
  ['Depois de doar', [
    ['✅', 'Cuidados imediatos', ['Permaneça no hemocentro por 15 minutos e aceite o lanche oferecido.', 'Mantenha o curativo por pelo menos 4 horas.', 'Hidrate-se bem, especialmente nas primeiras 4 horas.', 'Não fume por cerca de 2 horas e evite álcool por 12 horas.']],
    ['🚗', 'Dirigir e esforço físico', ['Dirigir (carro): aguarde pelo menos 1 hora.', 'Esforço físico e academia: evite por 12 horas (idealmente 24h).']],
    ['👷', 'Profissões e esportes', ['Motoristas de ônibus/caminhão e operadores de máquinas: aguardar 12 horas.', 'Atletas (ciclismo, natação, mergulho, competição): aguardar 24 horas.']],
    ['🚨', 'Nos dias seguintes', ['Se tiver febre, diarreia ou sintomas de infecção em até 7 a 14 dias, comunique o hemocentro.']]]],
];

async function telaGuia(ctx) {
  const { f, rol } = tela(ctx, 'Guia de doação', { nav: 'doador', aba: 'lista' });
  const col = lista(rol, 'Guia de doação', 16, 20, ctx, 'lista');
  for (const [fase, topicos] of GUIA) {
    por(col, await texto(fase, 'titleLarge', V['on-background']), 'FILL');
    for (const [emoji, titulo, itens] of topicos) {
      const c = cartao(col, 'Tópico · ' + titulo, { gap: 8 });
      const t = bloco(c, 'Título', { dir: 'H', gap: 8 }, 'FILL');
      por(t, await texto(emoji, 'titleMedium', null, { nome: 'Emoji' }));
      por(t, await texto(titulo, 'titleMedium', V['on-surface']), 'FILL');
      for (const it of itens) por(c, await texto('•  ' + it, 'bodyMedium', V['on-surface-variant']), 'FILL');
    }
  }
  por(col, await texto('⚠️ Informações orientativas, baseadas no Ministério da Saúde e em hemocentros oficiais. A avaliação final é feita por um profissional no dia da doação.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  return f;
}

const MITOS = [
  ['Doar sangue engorda ou emagrece.', 'Mito', 'Não faz nem uma coisa nem outra. O líquido é reposto pelo corpo em cerca de 24 horas.'],
  ['Mulher menstruada não pode doar.', 'Mito', 'Pode, sim! A perda menstrual já é prevista pelo corpo. Quem usa anticoncepcional ou DIU também está liberada.'],
  ['Quem fez tatuagem recente pode doar normalmente.', 'Mito', 'Quem fez tatuagem, maquiagem definitiva ou micropigmentação há menos de 12 meses precisa aguardar esse período antes de doar.'],
  ['Doar sangue vicia.', 'Mito', 'Não existe nenhuma dependência ligada ao ato de doar.'],
  ['Preciso estar em jejum para doar.', 'Mito', 'É o contrário! Você deve estar alimentado. Doar em jejum aumenta o risco de passar mal.'],
  ['Doar enfraquece o organismo ou deixa o sangue "mais fraco".', 'Mito', 'O corpo repõe o volume rapidamente — a coleta é menos de 10% do seu sangue. Não há enfraquecimento.'],
  ['Quem toma remédio nunca pode doar.', 'Mito', 'Depende do remédio. Muitos não impedem (como anticoncepcional). Antibióticos e anti-inflamatórios pedem um tempo de espera. Informe sempre na triagem.'],
  ['Quem tem pressão alta ou diabetes não pode doar.', 'Depende', 'Se a hipertensão ou o diabetes estiverem controlados e sem complicações, geralmente é possível doar. A avaliação é feita na triagem.'],
  ['Já tive dengue, nunca mais posso doar.', 'Mito', 'Falso. Após a recuperação e um período de espera, você volta a poder doar.'],
  ['Quem já teve hepatite depois dos 11 anos não pode doar.', 'Verdade', 'Segundo a legislação, quem teve hepatite viral após os 11 anos de idade fica impedido de doar.'],
  ['Gripe ou febre não atrapalham a doação.', 'Mito', 'Quem está com gripe, resfriado ou febre deve aguardar a recuperação antes de doar.'],
  ['Uma doação ajuda várias pessoas.', 'Verdade', 'O sangue é separado em componentes — uma doação pode salvar até 4 vidas.'],
  ['Todo tipo sanguíneo é bem-vindo.', 'Verdade', 'Todos são importantes! O O– é o doador universal e o mais requisitado em emergências, mas os tipos mais comuns (O+ e A+) também são muito usados.'],
  ['Pessoas com tatuagem antiga (mais de 1 ano) não podem doar.', 'Mito', 'Podem. O impedimento é só para tatuagens recentes (menos de 12 meses), não importa a quantidade.'],
  ['Idosos não podem doar sangue.', 'Mito', 'É possível doar até os 69 anos, desde que a primeira doação tenha sido feita antes dos 60.'],
  ['Posso doar quantas vezes eu quiser no ano.', 'Mito', 'Há um intervalo: homens a cada 60 dias (até 4x/ano) e mulheres a cada 90 dias (até 3x/ano), para o corpo repor o ferro.'],
];

async function telaMitos(ctx) {
  const { f, rol } = tela(ctx, 'Mitos e verdades', { nav: 'doador', aba: 'lista' });
  const col = lista(rol, 'Mitos e verdades', 14, 20, ctx, 'lista');
  por(col, await texto('Muita gente deixa de doar por causa de informações erradas. Veja o que é mito e o que é verdade.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  for (const [pergunta, veredito, explicacao] of MITOS) {
    const c = cartao(col, 'Mito · ' + pergunta.slice(0, 30));
    por(c, await texto('“' + pergunta + '”', 'titleItalic', V['on-surface']), 'FILL');
    espaco(c, 8);
    por(c, SELO[veredito].createInstance());
    espaco(c, 10);
    por(c, await texto(explicacao, 'bodyMedium', V['on-surface-variant']), 'FILL');
  }
  por(col, await texto('⚠️ Conteúdo baseado em informações do Ministério da Saúde e de hemocentros oficiais (Hemominas, Hemoce, Pró-Sangue). Em caso de dúvida sobre seu caso, consulte o hemocentro antes de doar.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  return f;
}

async function telaConfiguracao(ctx, nomeTela, nomeConta, emailConta, tipo, aba) {
  const { f, rol } = tela(ctx, nomeTela, { nav: tipo, aba: aba });
  const col = lista(rol, 'Configuração', 20, 20);
  const conta = bloco(col, 'Conta', { dir: 'V', gap: 12 }, 'FILL');
  por(conta, await texto('Conta', 'bodyMedium', V['on-surface-variant']));
  const card = bloco(conta, 'Card conta', { dir: 'H', pad: 14, radius: 14, fill: tinta(V.surface), main: 'SPACE_BETWEEN', cross: 'CENTER', gap: 8 }, 'FILL');
  const tx = bloco(card, 'Dados', { dir: 'V' }, 'FILL');
  por(tx, await texto(nomeConta, 'bodyLargeBold', V['on-surface']), 'FILL');
  por(tx, await texto(emailConta, 'bodySmall', V['on-surface-variant']), 'FILL');
  ctx.links.push([toque(card, botaoContorno('Sair', { icone: true })), 'login']);
  const pref = bloco(col, 'Preferências', { dir: 'V', gap: 16 }, 'FILL');
  por(pref, await texto('Preferências', 'bodyMedium', V['on-surface-variant']));
  for (const [ic, titulo, on] of [['temaEscuro', 'Tema escuro', ctx.tema === 'escuro'], ['notificacao', 'Notificações', true]]) {
    const l = bloco(pref, 'Preferência · ' + titulo, { dir: 'H', main: 'SPACE_BETWEEN', cross: 'CENTER' }, 'FILL');
    const esq = bloco(l, 'Rótulo', { dir: 'H', gap: 12, cross: 'CENTER' });
    por(esq, icone(ic, V['on-surface']));
    por(esq, await texto(titulo, 'bodyLarge', V['on-surface']));
    por(l, SW[on ? 'Sim' : 'Não'].createInstance());
  }
  const sobre = bloco(col, 'Sobre o app', { dir: 'V', gap: 16 }, 'FILL');
  por(sobre, await texto('Sobre o app', 'bodyMedium', V['on-surface-variant']));
  const v = bloco(sobre, 'Versão', { dir: 'H', main: 'SPACE_BETWEEN' }, 'FILL');
  por(v, await texto('Versão', 'bodyLarge', V['on-surface']));
  por(v, await texto('1.0.0', 'bodyLarge', V['on-surface-variant']));
  por(sobre, await texto('Desenvolvido por', 'bodyMedium', V['on-surface']));
  for (const [n, e] of [['Henrique Carneiro Ribeiro', 'SM (Scrum Master)'], ['Rinaldo Pereira de Andrade Júnior', 'Dev Front'],
    ['Lucas Pereira Vietiez', 'Dev Back'], ['Thiago Louback Bonifácio', 'UX/UI']]) {
    const d = bloco(sobre, 'Dev · ' + n, { dir: 'V', gap: 1 }, 'FILL');
    por(d, await texto(n, 'padrao15', V['on-surface']));
    por(d, await texto(e, 'padrao13', V['vermelho-acao']));
  }
  return f;
}

const NIVEIS = [['estavel', '🟢 Estável'], ['alerta', '🟡 Alerta'], ['critico', '🔴 Crítico'], ['emergencia', '⚫ Emergência']];

// estado 0 = inicial (como o app abre), 1 = necessidade publicada, 2 = doadores compatíveis
async function telaPainel(ctx, estado) {
  const cor = { estavel: V.verde, alerta: V['amarelo-alerta'], critico: V.vermelho, emergencia: V['roxo-emergencia'] };
  const nomes = ['Estoque e necessidades', 'Estoque — necessidade publicada', 'Estoque — doadores compatíveis'];
  const { f, rol } = tela(ctx, nomes[estado], { nav: 'hospital', aba: 'painel' });
  const col = lista(rol, 'Estoque e necessidades', 14, 24, ctx, 'painel');
  por(col, await texto('Gerencie seu estoque, publique necessidades e encontre doadores compatíveis.', 'bodyMedium', V['on-surface-variant']), 'FILL');

  const est = cartao(col, 'Termômetro de estoque');
  por(est, await texto('🌡️ Termômetro de estoque', 'titleMedium', V['on-surface']), 'FILL');
  espaco(est, 4);
  por(est, await texto('Toque no nível de cada tipo sanguíneo para atualizar.', 'bodySmall', V['on-surface-variant']), 'FILL');
  const tipos = ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'];
  for (let i = 0; i < tipos.length; i++) {
    espaco(est, 14);
    por(est, await texto(tipos[i], 'bodyLargeBold', cor.estavel));
    espaco(est, 6);
    const chips = bloco(est, 'Níveis · ' + tipos[i], { dir: 'H', gap: 8, wrap: true, wrapGap: 0 }, 'FILL');
    for (const [, rot] of NIVEIS) toque(chips, chip(rot, false), null, 8);
    if (i < tipos.length - 1) espaco(est, 4);
  }

  const pub = cartao(col, 'Publicar necessidade');
  por(pub, await texto('Publicar necessidade', 'titleMedium', V['on-surface']), 'FILL');
  espaco(pub, 12);
  por(pub, campo('Tipo sanguíneo necessário', { fim: 'seta' }), 'FILL');
  espaco(pub, 12);
  por(pub, campo('Urgência', { valor: '🟡 Alerta — estoque baixo', fim: 'seta', fundo: V.surface }), 'FILL');
  espaco(pub, 14);
  const bPub = toque(pub, botao('Publicar'), 'FILL');
  if (estado === 0) ctx.links.push([bPub, 'estoque1', true]);
  if (estado > 0) {
    espaco(pub, 8);
    por(pub, await texto('✅ Necessidade publicada com sucesso.', 'bodyMedium', V.verde), 'FILL');
  }

  const nec = cartao(col, 'Minhas necessidades');
  por(nec, await texto('Minhas necessidades', 'titleMedium', V['on-surface']), 'FILL');
  if (estado === 0) {
    por(nec, await texto('Nenhuma necessidade publicada ainda.', 'bodyMedium', V['on-surface-variant']), 'FILL');
    nec.children[nec.children.length - 1].name = 'Vazio';
    nec.itemSpacing = 8;
  } else {
    espaco(nec, 12);
    const l = bloco(nec, 'Necessidade · A+', { dir: 'H', main: 'SPACE_BETWEEN', cross: 'CENTER' }, 'FILL');
    const esq = bloco(l, 'Tipo', { dir: 'H', cross: 'CENTER' });
    por(esq, await texto('A+', 'titleMedium', cor.alerta));
    por(esq, await texto('  🟡 Alerta', 'bodyMedium', V['on-surface-variant']));
    const ver = toque(l, botaoContorno('Ver doadores'));
    if (estado === 1) ctx.links.push([ver, 'estoque2', true]);
  }

  if (estado === 2) {
    const m = cartao(col, 'Doadores compatíveis');
    const mt = bloco(m, 'Topo', { dir: 'H', gap: 8 }, 'FILL');
    por(mt, await texto('Doadores compatíveis com A+', 'titleMedium', V['on-surface']), 'FILL');
    ctx.links.push([toque(mt, botaoTexto('Fechar')), 'estoque1', true]);
    espaco(m, 4);
    por(m, await texto('Tipos que podem doar: O-, O+, A-, A+', 'bodyMedium', V['on-surface-variant']), 'FILL');
    for (const [nome, tp, cidade, tel, ident] of [
      ['Ana Beatriz Souza', 'O-', 'São Paulo, SP', '(11) 91234-5678', true],
      ['Carlos Eduardo Lima', 'O+', 'São Paulo, SP', '', false],
      ['Fernanda Costa', 'A+', 'Campinas, SP', '(19) 99876-5432', true],
      ['João Pedro Alves', 'A-', 'São Paulo, SP', '', false]]) {
      espaco(m, 12);
      const d = bloco(m, 'Doador · ' + nome, { dir: 'V', pad: 12, radius: 12, fill: tinta(V.background) }, 'FILL');
      const t = bloco(d, 'Identificação', { dir: 'H', gap: 10, cross: 'CENTER' }, 'FILL');
      const badge = bloco(t, 'Tipo sanguíneo', { dir: 'H', pad: [4, 10, 4, 10], radius: 8, fill: SOLIDO('#C8102E', 0.15) });
      por(badge, await texto(tp, 'labelLargeBold', V.vermelho));
      const nc = bloco(t, 'Nome', { dir: 'V' }, 'FILL');
      por(nc, await texto(nome, 'bodyLargeSemi', V['on-surface']), 'FILL');
      por(nc, await texto(cidade, 'bodySmall', V['on-surface-variant']), 'FILL');
      espaco(d, 8);
      if (ident) {
        const tl = bloco(d, 'Telefone', { dir: 'H', gap: 6, cross: 'CENTER' });
        por(tl, icone('telefone', V['on-surface-variant']));
        por(tl, await texto(tel, 'bodyMedium', V['on-surface-variant']));
        espaco(d, 8);
        const conf = toque(d, botao('Confirmar doação', { cor: V.verde, raio: 12 }));
        if (nome === 'Ana Beatriz Souza') ctx.links.push([conf, 'historico']);
      } else {
        const lk = bloco(d, 'Contato protegido', { dir: 'H', gap: 6, cross: 'CENTER' });
        por(lk, icone('cadeado', V['on-surface-variant']));
        por(lk, await texto('Contato protegido', 'bodySmall', V['on-surface-variant']));
      }
    }
  }
  return f;
}

// Contas de exemplo mostradas nas telas (o app não tem mais contas de teste: são só dados ilustrativos).
const CONTA_DOADOR = ['Maria Doadora', 'maria@email.com'];
const CONTA_HOSPITAL = ['Hospital São Lucas', 'contato@saolucas.org.br'];

// SeloVerificacao (PerfilInstituicaoScreen.kt): verde 15%, raio total, ícone 24 + rótulo.
async function seloVerificado(pai) {
  const s = bloco(pai, 'Selo · Verificado', { dir: 'H', gap: 4, pad: [4, 10, 4, 10], radius: 100, cross: 'CENTER', fill: SOLIDO('#35C47A', 0.15) });
  por(s, icone('check', V.verde));
  por(s, await texto('Verificado', 'labelMediumBold', V.verde));
  return s;
}

// Hub do hospital (PainelHospitalHubScreen.kt) — como o app abre logo após o cadastro.
async function telaHub(ctx) {
  const { f, rol } = tela(ctx, 'Hospital — Início', { nav: 'hospital', aba: 'painel' });
  const col = lista(rol, 'Hemare Hospital', 14, 24, ctx);
  const topo = bloco(col, 'Hospital', { dir: 'H', main: 'SPACE_BETWEEN', cross: 'CENTER' }, 'FILL');
  const id = bloco(topo, 'Identificação', { dir: 'V', gap: 4 });
  por(id, await texto(CONTA_HOSPITAL[0], 'titleMedium', V['on-surface']));
  await seloVerificado(id);
  ctx.links.push([toque(topo, botaoTexto('Sair')), 'login']);

  const stats = bloco(col, 'Resumo', { dir: 'H', gap: 10 }, 'FILL');
  for (const [valor, rotulo] of [['0', 'Necessidades'], ['0', 'Doações'], ['0', 'Em alerta']]) {
    const c = bloco(stats, 'Resumo · ' + rotulo, { dir: 'V', pad: [14, 0, 14, 0], cross: 'CENTER', radius: 14, fill: tinta(V.surface) }, 'FILL');
    sombra(c);
    por(c, await texto(valor, 'headlineSmall', V['on-surface'], { align: 'CENTER' }));
    por(c, await texto(rotulo, 'labelSmall', V['on-surface-variant'], { align: 'CENTER' }));
  }

  for (const [dest, ic, t, d] of [
    ['estoque', 'estoque', 'Estoque e necessidades', 'Termômetro, publicar necessidade e buscar doadores'],
    ['historico', 'historico', 'Histórico de doações', 'Doações já confirmadas por este hospital'],
    ['perfil', 'perfil', 'Perfil da instituição', 'CNPJ, CNES e endereço cadastrados'],
    ['plano', 'plano', 'Plano', 'Assinatura atual e uso do mês']]) {
    const i = ITEM.createInstance();
    por(col, i, 'FILL');
    setTxt(i, 'Título', t); setTxt(i, 'Descrição', d);
    let feito = false;
    try { feito = setProp(i, 'Ícone', IC[ic].id); } catch (e) { /* troca direta */ }
    if (!feito) acharNo(i, 'Ícone').swapComponent(IC[ic]);
    pintarVetores(acharNo(i, 'Ícone'), V.vermelho);
    i.name = 'Item · ' + t;
    ctx.links.push([i, dest]);
  }
  return f;
}

// HistoricoDoacoesScreen.kt — depois de confirmar a doação da Ana no match.
async function telaHistorico(ctx) {
  const { f, rol } = tela(ctx, 'Histórico de doações', { nav: 'hospital', aba: 'painel' });
  const col = lista(rol, 'Histórico de doações', 12, 24, ctx, 'painel');
  por(col, await texto('Toda doação confirmada em "Estoque e necessidades" fica registrada aqui.', 'bodyMedium', V['on-surface-variant']), 'FILL');
  for (const [nome, tipo, quando] of [['Ana Beatriz Souza', 'O-', '08/10 às 20:03'], ['Fernanda Costa', 'A+', '07/10 às 15:40']]) {
    const c = cartao(col, 'Doação · ' + nome, { dir: 'H', pad: 14, main: 'SPACE_BETWEEN', cross: 'CENTER' });
    const esq = bloco(c, 'Doador', { dir: 'H', gap: 10, cross: 'CENTER' });
    const badge = bloco(esq, 'Tipo sanguíneo', { dir: 'H', pad: [4, 10, 4, 10], radius: 8, fill: SOLIDO('#C8102E', 0.15) });
    por(badge, await texto(tipo, 'labelLargeBold', V.vermelho));
    const nc = bloco(esq, 'Nome', { dir: 'V' });
    por(nc, await texto(nome, 'bodyLargeSemi', V['on-surface']));
    por(nc, await texto(quando, 'bodySmall', V['on-surface-variant']));
    por(c, await texto('✓ Confirmada', 'bodySmallBold', V.verde));
  }
  return f;
}

// PerfilInstituicaoScreen.kt — dados informados no cadastro.
async function telaPerfil(ctx) {
  const { f, rol } = tela(ctx, 'Perfil da instituição', { nav: 'hospital', aba: 'painel' });
  const col = lista(rol, 'Perfil da instituição', 14, 24, ctx, 'painel');
  const c1 = cartao(col, 'Instituição', { gap: 4 });
  por(c1, await texto(CONTA_HOSPITAL[0], 'titleLarge', V['on-surface']), 'FILL');
  por(c1, await texto(CONTA_HOSPITAL[1], 'bodyMedium', V['on-surface-variant']), 'FILL');
  await seloVerificado(c1);
  const c2 = cartao(col, 'Dados do cadastro', { gap: 12 });
  for (const [rotulo, valor] of [['CNPJ', '11.222.333/0001-81'], ['CNES', '1234567'], ['Endereço', 'Rua da Aurora, 1200'],
    ['Bairro', 'Boa Vista'], ['Cidade / UF', 'Recife - PE'], ['CEP', '50050-000']]) {
    const l = bloco(c2, 'Linha · ' + rotulo, { dir: 'V' }, 'FILL');
    por(l, await texto(rotulo, 'bodySmall', V['on-surface-variant']), 'FILL');
    por(l, await texto(valor, 'bodyLarge', V['on-surface']), 'FILL');
  }
  por(col, await texto('Esses dados foram informados no cadastro da instituição. A edição de perfil chega junto com a camada de dados real (Domain/Data layer).', 'bodySmall', V['on-surface-variant']), 'FILL');
  return f;
}

// PlanoScreen.kt — Profissional, 1 de 30 necessidades usadas.
async function telaPlano(ctx) {
  const { f, rol } = tela(ctx, 'Plano', { nav: 'hospital', aba: 'painel' });
  const col = lista(rol, 'Plano', 14, 24, ctx, 'painel');
  const atual = cartao(col, 'Plano atual');
  por(atual, await texto('Plano atual', 'bodySmall', V['on-surface-variant']), 'FILL');
  por(atual, await texto('Profissional', 'titleLarge', V['on-surface']), 'FILL');
  por(atual, await texto('R$ 249/mês · renova em 14 dias', 'bodyMedium', V['on-surface-variant']), 'FILL');
  espaco(atual, 12);
  const barra = bloco(atual, 'Uso do mês', { dir: 'H', radius: 4, clip: true, fill: tinta(V['surface-variant']) }, 'FILL');
  barra.counterAxisSizingMode = 'FIXED';
  barra.resize(339, 8);
  barra.layoutSizingHorizontal = 'FILL';
  const usado = bloco(barra, 'Usado', { dir: 'H', fill: tinta(V.vermelho) });
  fixo(usado, Math.round(339 / 30), 8);
  espaco(atual, 6);
  por(atual, await texto('1 de 30 necessidades publicadas este mês', 'bodySmall', V['on-surface-variant']), 'FILL');

  por(col, await texto('Planos disponíveis', 'titleMedium', V['on-surface']), 'FILL');
  for (const [nome, desc, eAtual] of [['Básico', '1 necessidade/mês · grátis', false],
    ['Profissional', 'Necessidades ilimitadas · R$ 249/mês', true], ['Rede / Secretaria', 'Múltiplas unidades · fale conosco', false]]) {
    const c = cartao(col, 'Plano · ' + nome, { dir: 'H', main: 'SPACE_BETWEEN', cross: 'CENTER' });
    const tx = bloco(c, 'Textos', { dir: 'V' });
    por(tx, await texto(nome, 'titleMedium', V['on-surface']));
    por(tx, await texto(desc, 'bodySmall', V['on-surface-variant']));
    if (eAtual) por(c, await texto('Atual', 'labelLargeBold', V.verde));
  }
  por(col, await texto('Assinaturas de hospitais e hemocentros sustentam o Hemare — o app do doador continua gratuito.', 'bodySmall', V['on-surface-variant']), 'FILL');
  return f;
}

// ---------------------------------------------------------------- páginas
async function secao(pai, titulo, desc) {
  const s = bloco(pai, titulo, { dir: 'V', gap: 24 }, 'FILL');
  por(s, await textoDoc(titulo, 32, 'bold'));
  if (desc) por(s, await textoDoc(desc, 14, 'regular', '#8A6B6F'), 'FILL');
  return s;
}

async function paginaDS(pg) {
  const raiz = figma.createFrame();
  pg.appendChild(raiz);
  raiz.name = 'Design System — Hemare Mobile';
  raiz.resize(1560, 100);
  raiz.layoutMode = 'VERTICAL';
  raiz.counterAxisSizingMode = 'FIXED';
  raiz.primaryAxisSizingMode = 'AUTO';
  raiz.itemSpacing = 72;
  raiz.paddingTop = raiz.paddingBottom = raiz.paddingLeft = raiz.paddingRight = 72;
  raiz.fills = [SOLIDO('#FFFFFF')];
  raiz.cornerRadius = 24;
  por(raiz, await textoDoc('Design System — Hemare Mobile', 56, 'bold', '#C8102E'));
  por(raiz, await textoDoc('Medidas e cores tiradas do app rodando num Pixel (411×923 dp, Material 3) e do código Compose (ui/theme).', 18, 'regular', '#8A6B6F'), 'FILL');
  const cores = await secao(raiz, 'Cores', '"marca/*" são fixas; "tema/*" seguem lightColorScheme / darkColorScheme (outline e outline-variant vêm do Material 3 padrão).');
  const linhaModos = bloco(cores, 'Modos', { dir: 'H', gap: 32 }, 'FILL');
  for (const [tema, rot, idx] of [['claro', 'Claro', 1], ['escuro', 'Escuro (padrão do app)', 2]]) {
    const painel = bloco(linhaModos, 'Paleta · ' + rot, { dir: 'V', gap: 16, pad: 24, radius: 16, fill: tinta(V.background) }, 'FILL');
    por(painel, await texto('Tema ' + rot, 'titleLarge', V['on-background']));
    const grade = bloco(painel, 'Amostras', { dir: 'H', gap: 16, wrap: true, wrapGap: 16 }, 'FILL');
    for (const linha of CORES) {
      const am = bloco(grade, linha[0], { dir: 'V', gap: 6 });
      const r = figma.createRectangle();
      r.name = 'Cor'; r.resize(104, 56); r.cornerRadius = 10;
      r.fills = [tinta(V[linha[0].split('/')[1]])];
      r.strokes = [SOLIDO('#000000', 0.08)];
      am.appendChild(r);
      por(am, await texto(linha[0], 'bodySmall', V['on-background']));
      por(am, await texto(linha[idx], 'bodySmall', V['on-surface-variant']));
    }
    aplicarTema(painel, tema);
  }
  const tipo = await secao(raiz, 'Tipografia', 'Fonte ' + FONTE + '. "Corpo/Grande" (bodyLarge do Type.kt) tem a entrelinha cortada pelo Compose: 1 linha = 19 dp.');
  for (const k of Object.keys(ESTILOS)) {
    const e = ESTILOS[k];
    const l = bloco(tipo, 'Estilo · ' + e[0], { dir: 'H', gap: 32, cross: 'CENTER' }, 'FILL');
    const a = await texto('Doe sangue, doe esperança', k, SOLIDO('#2B0D10'));
    por(l, a);
    a.textAutoResize = 'HEIGHT';
    a.resize(420, a.height);
    por(l, await textoDoc(e[0] + '  ·  ' + e[2] + '/' + e[3] + '  ·  ' + F[e[1]].style, 14, 'regular', '#8A6B6F'));
  }
  const ics = await secao(raiz, 'Ícones', 'Material Icons (Filled) usados no app.');
  await criarIcones(bloco(ics, 'Ícones', { dir: 'H', gap: 12, wrap: true, wrapGap: 12 }, 'FILL'));
  const comp = await secao(raiz, 'Componentes', 'Todas as telas usam instâncias destes componentes.');
  const g = bloco(comp, 'Componentes', { dir: 'H', gap: 48, wrap: true, wrapGap: 48 }, 'FILL');
  await criarStatusBar(g);
  await criarCabecalho(g);
  await criarBotoes(g);
  await criarCampo(g);
  await criarNav(g, 'Navegação/Doador', ABAS_DOADOR, NAV_D);
  await criarNav(g, 'Navegação/Hospital', ABAS_HOSPITAL, NAV_H);
  await criarItemLista(g);
  await criarEstatistica(g);
  await criarChips(g);
  await criarOpcoes(g);
  await criarSelos(g);
  await criarSwitch(g);
  return raiz;
}

async function paginaCapa(pg) {
  const f = figma.createFrame();
  pg.appendChild(f);
  f.name = 'Capa — Hemare Mobile';
  f.resize(1600, 900);
  f.fills = [SOLIDO('#C8102E')];
  f.clipsContent = true;
  const g = svgGotas(640, [[340, 330, 380, 0.14], [150, 470, 200, 0.22], [500, 130, 130, 0.26]], 135);
  g.name = 'Gotas';
  f.appendChild(g); g.x = 1000; g.y = 140;
  const t1 = await textoDoc('🩸 Hemare Mobile', 120, 'bold', '#FFFFFF');
  f.appendChild(t1); t1.x = 120; t1.y = 250;
  const t2 = await textoDoc('Design do app Android de doação de sangue', 40, 'regular', '#FFD9DF');
  f.appendChild(t2); t2.x = 124; t2.y = 420; t2.resize(860, t2.height); t2.textAutoResize = 'HEIGHT';
  const t3 = await textoDoc('Fiel ao app rodando no Android (411×923 dp) · Tema escuro e claro · Protótipo navegável', 26, 'medium', '#FFFFFF');
  f.appendChild(t3); t3.x = 124; t3.y = 540; t3.resize(820, t3.height); t3.textAutoResize = 'HEIGHT';
  const t4 = await textoDoc('github.com/Henrique-CR0/Projeto-Hemare-Mobile', 18, 'regular', '#FFD9DF');
  f.appendChild(t4); t4.x = 124; t4.y = 800;
  try { await figma.setFileThumbnailNodeAsync(f); } catch (e) { /* ok */ }
  return f;
}

async function ligar(ctx, mapa) {
  let ok = 0;
  for (const [no, dest, manterRolagem] of ctx.links) {
    try {
      const acao = dest === 'BACK' ? { type: 'BACK' } : {
        type: 'NODE', destinationId: mapa[dest].id, navigation: 'NAVIGATE',
        transition: { type: 'DISSOLVE', easing: { type: 'EASE_OUT' }, duration: 0.2 },
        preserveScrollPosition: !!manterRolagem,
      };
      await no.setReactionsAsync([{ trigger: { type: 'ON_CLICK' }, actions: [acao] }]);
      ok++;
    } catch (e) { /* ignora */ }
  }
  return ok;
}

async function paginaTelas(pg, tema) {
  const escuro = tema === 'escuro';
  try { pg.backgrounds = [SOLIDO(escuro ? '#0E0E0E' : '#F3EDEE')]; } catch (e) { /* ok */ }
  const corTitulo = escuro ? '#FFFFFF' : '#2B0D10';
  const ctx = { tema: tema, links: [] };
  const linhas = [
    ['Autenticação', [['login', telaLogin], ['cadastroDoador', telaCadastroDoador], ['cadastroHospital', telaCadastroHospital]]],
    ['Doador — abas Início, Lista e Configuração', [
      ['inicio', telaInicio], ['lista', telaLista],
      ['possoDoar', (c) => telaPossoDoar(c, false)], ['possoDoarResultado', (c) => telaPossoDoar(c, true)],
      ['ondeDoar', telaOndeDoar], ['guia', telaGuia], ['mitos', telaMitos],
      ['config', (c) => telaConfiguracao(c, 'Configuração — Doador', CONTA_DOADOR[0], CONTA_DOADOR[1], 'doador', 'config')]]],
    ['Hospital — abas Hospital e Configuração', [
      ['painel', telaHub],
      ['estoque', (c) => telaPainel(c, 0)], ['estoque1', (c) => telaPainel(c, 1)], ['estoque2', (c) => telaPainel(c, 2)],
      ['historico', telaHistorico], ['perfil', telaPerfil], ['plano', telaPlano],
      ['configHosp', (c) => telaConfiguracao(c, 'Configuração — Hospital', CONTA_HOSPITAL[0], CONTA_HOSPITAL[1], 'hospital', 'configHosp')]]],
  ];
  const titulo = await textoDoc('Hemare Mobile — Tema ' + (escuro ? 'escuro (padrão do app)' : 'claro'), 64, 'bold', escuro ? '#E8112D' : '#C8102E');
  pg.appendChild(titulo); titulo.x = 0; titulo.y = -260;
  const nota = await textoDoc('Telas no tamanho do celular real (411×923 dp). Protótipo: ▶ Present — começa no Login. Em Prototype › Device escolha um Android/Pixel.', 20, 'regular', escuro ? '#A0A0A0' : '#8A6B6F');
  pg.appendChild(nota); nota.x = 0; nota.y = -170; nota.resize(1600, nota.height); nota.textAutoResize = 'HEIGHT';
  const mapa = {};
  let y = 0;
  for (const [tituloLinha, telas] of linhas) {
    const lt = await textoDoc(tituloLinha, 36, 'bold', corTitulo);
    pg.appendChild(lt); lt.x = 0; lt.y = y;
    y += 80;
    let x = 0;
    for (const [k, fn] of telas) {
      const f = await fn(ctx);
      pg.appendChild(f);
      f.x = x; f.y = y;
      aplicarTema(f, tema);
      mapa[k] = f;
      x += W + 80;
    }
    y += H + 200;
  }
  const ligados = await ligar(ctx, mapa);
  try { pg.flowStartingPoints = [{ nodeId: mapa.login.id, name: 'Hemare Mobile — ' + (escuro ? 'tema escuro' : 'tema claro') }]; } catch (e) { /* ok */ }
  return { mapa: mapa, ligados: ligados, total: ctx.links.length };
}
