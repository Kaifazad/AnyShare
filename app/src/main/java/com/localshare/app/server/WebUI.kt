package com.localshare.app.server

object WebUI {
    fun getHtml(deviceName: String, needsAuth: Boolean): String {
        val escapedName = deviceName.replace("'", "\\'")
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#f5f5f8">
<title>LocalShare</title>
<link rel="icon" type="image/png" href="/logo.png">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=Space+Mono:wght@400;700&display=swap" rel="stylesheet">
<style>
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}

:root{
  --bg:       #f5f5f8;
  --bg2:      #ffffff;
  --bg3:      #ebebf0;
  --bg4:      #e0e0e8;
  --line:     rgba(0,0,0,0.08);
  --line2:    rgba(0,0,0,0.14);
  --txt:      #0a0a14;
  --txt2:     #5a5a70;
  --txt3:     #9090a8;
  --accent:   #6c63ff;
  --accent2:  #8b5cf6;
  --green:    #22c55e;
  --pink:     #ec4899;
  --amber:    #f59e0b;
  --blue:     #3b82f6;
  --red:      #ef4444;
  --r4:       4px;
  --r8:       8px;
  --r12:      12px;
  --r16:      16px;
  --r999:     999px;
  --font:     'DM Sans', sans-serif;
  --mono:     'Space Mono', monospace;
  --topbar-bg: rgba(245,245,248,0.85);
}

html{scroll-behavior:smooth}

body{
  font-family:var(--font);
  background:var(--bg);
  color:var(--txt);
  min-height:100vh;
  -webkit-font-smoothing:antialiased;
  overflow-x:hidden;
}

/* subtle grid bg */
body::before{
  content:'';
  position:fixed;inset:0;
  background-image:
    linear-gradient(rgba(108,99,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(108,99,255,0.03) 1px, transparent 1px);
  background-size:40px 40px;
  pointer-events:none;
  z-index:0;
}

/* ── SCROLLBAR ── */
::-webkit-scrollbar{width:5px}
::-webkit-scrollbar-track{background:transparent}
::-webkit-scrollbar-thumb{background:var(--bg4);border-radius:99px}

/* ── AUTH SCREEN ── */
#authScreen{
  position:fixed;inset:0;
  display:flex;align-items:center;justify-content:center;
  z-index:999;
  background:transparent;
  backdrop-filter:blur(10px);
}

.auth-wrap{
  width:100%;max-width:400px;
  padding:36px 32px;
  background:var(--bg2);
  border:1px solid var(--line2);
  border-radius:var(--r16);
  animation:fadeUp .5s ease both;
}

.auth-header{
  display:flex;align-items:center;justify-content:space-between;
  margin-bottom:32px;
}

.auth-logo{
  display:flex;align-items:center;gap:10px;
}
.auth-logo-icon{
  width:36px;height:36px;
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  overflow:hidden;
}
.auth-logo-text{
  font-size:18px;font-weight:600;letter-spacing:-0.3px;
}

.auth-label{
  font-size:12px;font-weight:500;color:var(--accent);
  font-family:var(--mono);
  text-transform:uppercase;letter-spacing:1.5px;
  margin-bottom:10px;
}
.auth-heading{
  font-size:24px;font-weight:600;letter-spacing:-0.5px;
  margin-bottom:8px;
}
.auth-sub{
  font-size:14px;color:var(--txt2);margin-bottom:28px;
  line-height:1.5;
}

.pin-row{
  display:flex;gap:12px;margin-bottom:24px;
  justify-content:center;
}
.pin-row input{
  width:56px;height:56px;
  text-align:center;
  font-size:22px;font-weight:700;font-family:var(--mono);
  background:var(--bg3);
  border:none;
  box-shadow:inset 0 2px 4px rgba(0,0,0,0.06);
  border-radius:var(--r12);
  color:var(--txt);
  outline:none;
  transition:all .2s;
  -webkit-appearance:none;
  flex-shrink:0;
}
.pin-row input:focus{
  background:var(--bg4);
  box-shadow:inset 0 2px 4px rgba(0,0,0,0.06), 0 0 0 2px var(--accent);
}
.pin-row input.err{
  border-color:var(--red);
  animation:shake .4s ease;
}

.auth-btn{
  width:100%;height:48px;
  background:var(--accent);
  color:#fff;
  border:none;border-radius:var(--r12);
  font-size:15px;font-weight:600;font-family:var(--font);
  cursor:pointer;
  transition:opacity .2s, transform .15s;
}
.auth-btn:hover:not(:disabled){opacity:.9;transform:translateY(-1px)}
.auth-btn:active:not(:disabled){transform:translateY(0)}
.auth-btn:disabled{opacity:.4;cursor:not-allowed}

.auth-err{
  font-size:13px;color:var(--red);
  text-align:center;margin-top:12px;min-height:18px;
}

/* ── MAIN APP ── */
#mainApp{
  position:relative;z-index:1;
  display:none;
  min-height:100vh;
}
#mainApp.show{display:block;animation:fadeIn .3s ease}

/* ── TOPBAR ── */
.topbar{
  position:sticky;top:0;z-index:100;
  background:var(--topbar-bg);
  backdrop-filter:blur(20px);
  -webkit-backdrop-filter:blur(20px);
  border-bottom:1px solid var(--line);
  padding:0 24px;
  height:60px;
  display:flex;align-items:center;justify-content:space-between;
}

.topbar-left{display:flex;align-items:center;gap:12px}

.t-logo{
  width:30px;height:30px;
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  flex-shrink:0;
}

.t-name{font-size:16px;font-weight:600;letter-spacing:-0.3px}

.t-device{
  font-size:12px;font-family:var(--mono);
  color:var(--txt3);
  background:var(--bg3);
  padding:4px 10px;
  border-radius:var(--r999);
  border:1px solid var(--line);
}

.topbar-right{display:flex;align-items:center;gap:8px}

.live-pill{
  display:flex;align-items:center;gap:6px;
  font-size:12px;font-weight:500;
  color:var(--green);
  background:rgba(34,197,94,0.1);
  border:1px solid rgba(34,197,94,0.2);
  padding:5px 12px;
  border-radius:var(--r999);
}
.live-dot{
  width:6px;height:6px;
  background:var(--green);
  border-radius:50%;
  animation:blink 2s ease infinite;
}

.icon-btn{
  width:34px;height:34px;
  background:transparent;
  border:1px solid var(--line);
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  cursor:pointer;color:var(--txt2);
  transition:background .15s, color .15s, border-color .15s;
}
.icon-btn:hover{background:var(--bg3);color:var(--txt);border-color:var(--line2)}
.icon-btn svg{width:16px;height:16px;stroke:currentColor;stroke-width:2;fill:none;stroke-linecap:round;stroke-linejoin:round}

/* ── HERO SEARCH ── */
.hero{
  padding:32px 24px 0;
  max-width:900px;margin:0 auto;
}

.search-wrap{
  position:relative;margin-bottom:20px;
}
.search-wrap svg{
  position:absolute;left:16px;top:50%;transform:translateY(-50%);
  width:18px;height:18px;stroke:var(--txt3);stroke-width:2;fill:none;
  stroke-linecap:round;stroke-linejoin:round;
  pointer-events:none;
}
.search-wrap input{
  width:100%;height:48px;
  padding:0 16px 0 46px;
  background:var(--bg3);
  border:1.5px solid var(--line);
  border-radius:var(--r12);
  color:var(--txt);
  font-size:15px;font-family:var(--font);
  outline:none;
  transition:border-color .2s, box-shadow .2s;
}
.search-wrap input::placeholder{color:var(--txt3)}
.search-wrap input:focus{
  border-color:var(--accent);
  box-shadow:0 0 0 3px rgba(108,99,255,0.12);
}

/* ── FILTER BAR ── */
.filter-bar{
  display:flex;align-items:center;justify-content:space-between;
  gap:12px;flex-wrap:wrap;
  margin-bottom:24px;
}

.chips{display:flex;gap:6px;flex-wrap:wrap}

.chip{
  display:flex;align-items:center;gap:6px;
  padding:6px 14px;
  background:transparent;
  border:1px solid var(--line);
  border-radius:var(--r999);
  color:var(--txt2);
  font-size:13px;font-weight:500;font-family:var(--font);
  cursor:pointer;
  transition:all .15s;
  white-space:nowrap;
}
.chip:hover{border-color:var(--line2);color:var(--txt);background:var(--bg3)}
.chip.on{
  background:var(--accent);
  border-color:var(--accent);
  color:#fff;
}
.chip svg{width:14px;height:14px;stroke:currentColor;stroke-width:2;fill:none;stroke-linecap:round;stroke-linejoin:round}

.bar-right{display:flex;align-items:center;gap:6px}

.sort-sel{
  height:34px;
  padding:0 12px;
  background:var(--bg3);
  border:1px solid var(--line);
  border-radius:var(--r8);
  color:var(--txt2);
  font-size:13px;font-family:var(--font);
  outline:none;cursor:pointer;
  -webkit-appearance:none;
  transition:border-color .15s;
}
.sort-sel:hover{border-color:var(--line2)}

/* ── FILE GRID ── */
.main{
  padding:0 24px 120px;
  max-width:900px;margin:0 auto;
}

.file-grid{
  display:grid;
  grid-template-columns:repeat(auto-fill,minmax(260px,1fr));
  gap:10px;
}
.file-grid.list{grid-template-columns:1fr}

/* ── FILE CARD ── */
.fcard{
  background:var(--bg2);
  border:1px solid var(--line);
  border-radius:var(--r12);
  padding:14px;
  display:flex;align-items:center;gap:12px;
  cursor:pointer;
  position:relative;
  transition:border-color .2s, background .2s, transform .2s;
  overflow:hidden;
}
.fcard::after{
  content:'';
  position:absolute;inset:0;
  background:linear-gradient(135deg,rgba(108,99,255,0.04),transparent 60%);
  opacity:0;transition:opacity .2s;
  pointer-events:none;
}
.fcard:hover{
  border-color:rgba(108,99,255,0.35);
  background:var(--bg3);
  transform:translateY(-1px);
}
.fcard:hover::after{opacity:1}

.fcard.sel{
  border-color:var(--accent);
  background:rgba(108,99,255,0.08);
}

/* sel checkbox */
.fcheck{
  position:absolute;top:10px;left:10px;
  width:20px;height:20px;
  border-radius:50%;
  border:1.5px solid var(--line2);
  background:var(--bg3);
  display:flex;align-items:center;justify-content:center;
  opacity:0;
  transition:opacity .15s;
  z-index:2;
}
.fcard:hover .fcheck,.fcard.sel .fcheck{opacity:1}
.fcard.sel .fcheck{background:var(--accent);border-color:var(--accent)}
.fcheck svg{width:10px;height:10px;stroke:#fff;stroke-width:3;fill:none;stroke-linecap:round;stroke-linejoin:round;display:none}
.fcard.sel .fcheck svg{display:block}

/* file icon */
.ficon{
  width:44px;height:44px;flex-shrink:0;
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  overflow:hidden;
  position:relative;
}
.ficon svg{width:22px;height:22px;stroke:currentColor;stroke-width:2;fill:none;stroke-linecap:round;stroke-linejoin:round}
.ficon img{width:100%;height:100%;object-fit:cover}

.ficon.vid{background:rgba(236,72,153,0.12);color:var(--pink)}
.ficon.img{background:rgba(34,197,94,0.12);color:var(--green)}
.ficon.aud{background:rgba(245,158,11,0.12);color:var(--amber)}
.ficon.doc{background:rgba(59,130,246,0.12);color:var(--blue)}

.finfo{flex:1;min-width:0}
.fname{
  font-size:14px;font-weight:500;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
  margin-bottom:3px;
}
.fmeta{font-size:12px;color:var(--txt3)}

.factions{
  display:flex;gap:6px;flex-shrink:0;
}
.faction{
  width:30px;height:30px;
  background:var(--bg4);
  border:1px solid var(--line);
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  cursor:pointer;
  color:var(--txt2);
  text-decoration:none;
  transition:background .15s, color .15s, border-color .15s;
  flex-shrink:0;
}
.faction:hover{background:var(--accent);border-color:var(--accent);color:#fff}
.faction.play:hover{background:var(--pink);border-color:var(--pink);color:#fff}
.faction svg{width:14px;height:14px;stroke:currentColor;stroke-width:2;fill:none;stroke-linecap:round;stroke-linejoin:round}

/* list view adjustments */
.list .fcard{padding:12px 14px}
.list .ficon{width:38px;height:38px}

/* ── SECTION HEADERS ── */
.sec-label{
  font-size:11px;font-weight:500;
  font-family:var(--mono);
  color:var(--txt3);
  text-transform:uppercase;letter-spacing:1.5px;
  margin:24px 0 10px;
}

/* ── STATES ── */
.state{
  grid-column:1/-1;
  padding:80px 20px;
  text-align:center;
}
.state svg{width:40px;height:40px;stroke:var(--txt3);stroke-width:1.5;fill:none;margin-bottom:16px}
.state-t{font-size:16px;font-weight:500;margin-bottom:6px}
.state-d{font-size:14px;color:var(--txt2)}

.spinner{
  width:32px;height:32px;
  border:3px solid var(--bg3);
  border-top-color:var(--accent);
  border-radius:50%;
  animation:spin 1s linear infinite;
  margin:0 auto 12px;
}

/* ── FAB ── */
.fab{
  position:fixed;bottom:28px;left:50%;
  transform:translateX(-50%) translateY(80px);
  background:var(--bg3);
  border:1px solid var(--line2);
  border-radius:var(--r999);
  padding:10px 10px 10px 18px;
  display:flex;align-items:center;gap:10px;
  box-shadow:0 8px 32px rgba(0,0,0,0.4);
  z-index:90;
  transition:transform .35s cubic-bezier(.16,1,.3,1);
  white-space:nowrap;
}
.fab.show{transform:translateX(-50%) translateY(0)}
.fab-info{font-size:13px;font-weight:500;color:var(--txt2)}
.fab-count{
  font-family:var(--mono);
  font-size:13px;color:var(--txt);
}
.fab-clr{
  font-size:13px;color:var(--txt3);
  background:none;border:none;cursor:pointer;
  font-family:var(--font);
  padding:4px 8px;border-radius:var(--r8);
  transition:color .15s, background .15s;
}
.fab-clr:hover{color:var(--txt);background:var(--bg4)}
.fab-dl{
  display:flex;align-items:center;gap:6px;
  height:34px;padding:0 16px;
  background:var(--accent);color:#fff;
  border:none;border-radius:var(--r999);
  font-size:13px;font-weight:600;font-family:var(--font);
  cursor:pointer;
  transition:opacity .15s;
}
.fab-dl:hover{opacity:.9}
.fab-dl svg{width:13px;height:13px;stroke:#fff;stroke-width:2.5;fill:none;stroke-linecap:round;stroke-linejoin:round}

/* ── MODAL ── */
.overlay{
  position:fixed;inset:0;
  background:rgba(0,0,0,0.75);
  backdrop-filter:blur(8px);
  -webkit-backdrop-filter:blur(8px);
  z-index:500;
  display:none;
  align-items:center;justify-content:center;
  padding:20px;
}
.overlay.open{display:flex;animation:fadeIn .2s ease}

.modal{
  background:var(--bg2);
  border:1px solid var(--line2);
  border-radius:var(--r16);
  width:100%;max-width:860px;
  overflow:hidden;
  animation:slideUp .3s cubic-bezier(.16,1,.3,1);
}
.modal-top{
  display:flex;align-items:center;justify-content:space-between;
  padding:14px 18px;
  border-bottom:1px solid var(--line);
}
.modal-fname{
  font-size:14px;font-weight:500;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
  max-width:70%;
}
.modal-close{
  width:30px;height:30px;
  background:var(--bg3);border:1px solid var(--line);
  border-radius:var(--r8);
  display:flex;align-items:center;justify-content:center;
  cursor:pointer;color:var(--txt2);
  transition:background .15s, color .15s;
}
.modal-close:hover{background:var(--bg4);color:var(--txt)}
.modal-close svg{width:14px;height:14px;stroke:currentColor;stroke-width:2;fill:none;stroke-linecap:round;stroke-linejoin:round}
.modal-body{
  padding:20px;
  background:var(--bg);
  display:flex;align-items:center;justify-content:center;
  min-height:200px;
}
.modal-body video,.modal-body audio,.modal-body img{
  max-width:100%;max-height:72vh;
  border-radius:var(--r8);outline:none;display:block;
}

/* ── STATS BAR ── */
.stats{
  display:flex;gap:0;
  margin-bottom:24px;
  background:var(--bg2);
  border:1px solid var(--line);
  border-radius:var(--r12);
  overflow:hidden;
}
.stat{
  flex:1;padding:14px 16px;
  border-right:1px solid var(--line);
}
.stat:last-child{border-right:none}
.stat-v{
  font-size:20px;font-weight:600;font-family:var(--mono);
  color:var(--txt);margin-bottom:2px;
}
.stat-l{font-size:12px;color:var(--txt3)}

/* ── KEYFRAMES ── */
@keyframes fadeIn{from{opacity:0}to{opacity:1}}
@keyframes fadeUp{from{opacity:0;transform:translateY(16px)}to{opacity:1;transform:translateY(0)}}
@keyframes slideUp{from{opacity:0;transform:translateY(12px) scale(.98)}to{opacity:1;transform:translateY(0) scale(1)}}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes blink{0%,100%{opacity:1}50%{opacity:.4}}
@keyframes shake{0%,100%{transform:translateX(0)}20%,60%{transform:translateX(-5px)}40%,80%{transform:translateX(5px)}}

/* ── RESPONSIVE ── */
@media(max-width:600px){
  .topbar{padding:0 16px}
  .hero,.main{padding-left:16px;padding-right:16px}
  .t-device{display:none}
  .stat-v{font-size:17px}
  .auth-wrap{margin:0 16px;padding:28px 20px}
}

.hidden{display:none!important}
</style>
</head>
<body>

<!-- ── AUTH ── -->
<div id="authScreen">
  <div class="auth-wrap">
    <div class="auth-header" style="justify-content:center; margin-bottom:40px;">
      <div class="auth-logo">
        <div class="auth-logo-icon">
          <img id="authLogo" src="/logo.png" style="width:100%;height:100%;object-fit:cover;border-radius:var(--r8);">
        </div>
        <span class="auth-logo-text">LocalShare</span>
      </div>
    </div>
    <div class="auth-label">PIN Required</div>
    <div class="auth-heading">Enter your PIN</div>
    <p class="auth-sub"><b id="authDeviceName" style="color:var(--txt);">This device</b> is protected. Enter the 4-digit PIN set on the phone.</p>
    <div class="pin-row" id="pinRow">
      <input type="tel" maxlength="1" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="tel" maxlength="1" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="tel" maxlength="1" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="tel" maxlength="1" inputmode="numeric" pattern="[0-9]" autocomplete="off">
    </div>
    <button class="auth-btn" id="authBtn" disabled>Unlock</button>
    <div class="auth-err" id="authErr"></div>
  </div>
</div>

<!-- ── MAIN APP ── -->
<div id="mainApp">

  <header class="topbar">
    <div class="topbar-left">
      <div class="t-logo">
        <img id="navLogo" src="/logo.png" style="width:100%;height:100%;object-fit:cover;border-radius:var(--r8);">
      </div>
      <span class="t-name">LocalShare</span>
      <span class="t-device" id="topDevice">Pixel 7</span>
    </div>
    <div class="topbar-right">
      <div class="live-pill"><span class="live-dot"></span>Live</div>
      <button class="icon-btn" id="themeBtn" title="Toggle theme">
        <svg viewBox="0 0 24 24" id="themeIco"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
      </button>
    </div>
  </header>

  <div class="hero">
    <!-- stats -->
    <div class="stats" id="statsBar">
      <div class="stat"><div class="stat-v" id="statFiles">—</div><div class="stat-l">Files shared</div></div>
      <div class="stat"><div class="stat-v" id="statDevices">—</div><div class="stat-l">Connected</div></div>
      <div class="stat"><div class="stat-v" id="statPin">—</div><div class="stat-l">PIN protect</div></div>
    </div>

    <!-- search -->
    <div class="search-wrap">
      <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input type="text" id="searchInp" placeholder="Search files…" autocomplete="off">
    </div>

    <!-- filters -->
    <div class="filter-bar">
      <div class="chips" id="chips">
        <button class="chip on" data-cat="all">All</button>
        <button class="chip" data-cat="videos">
          <svg viewBox="0 0 24 24"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>Video
        </button>
        <button class="chip" data-cat="photos">
          <svg viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>Photos
        </button>
        <button class="chip" data-cat="audio">
          <svg viewBox="0 0 24 24"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>Audio
        </button>
        <button class="chip" data-cat="documents">
          <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>Docs
        </button>
        <button class="chip" data-cat="apps">
          <svg viewBox="0 0 24 24"><rect x="4" y="4" width="6" height="6" rx="1"/><rect x="14" y="4" width="6" height="6" rx="1"/><rect x="4" y="14" width="6" height="6" rx="1"/><rect x="14" y="14" width="6" height="6" rx="1"/></svg>Apps
        </button>
        <button class="chip" data-cat="custom_folders">
          <svg viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5.53C2 4.13 3.13 3 4.53 3h4.63l2.25 2.25H20a2 2 0 0 1 2 2v11.75z"/></svg>Folders
        </button>
      </div>
      <div class="bar-right">
        <select class="sort-sel" id="sortSel">
          <option value="name">Name</option>
          <option value="size">Size</option>
          <option value="date">Date</option>
        </select>
        <button class="icon-btn" id="viewBtn" title="Toggle view">
          <svg viewBox="0 0 24 24" id="viewIco"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
        </button>
      </div>
    </div>
  </div>

  <div class="main">
    <div class="file-grid" id="grid">
      <div class="state">
        <div class="spinner"></div>
        <div class="state-d">Loading files from device…</div>
      </div>
    </div>
  </div>

</div>

<!-- ── FAB ── -->
<div class="fab" id="fab">
  <span class="fab-count" id="fabCount">0</span>
  <span class="fab-info">selected</span>
  <button class="fab-clr" id="fabClr">Clear</button>
  <button class="fab-dl" id="fabDl">
    <svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
    Download ZIP
  </button>
</div>

<!-- ── MODAL ── -->
<div class="overlay" id="overlay">
  <div class="modal">
    <div class="modal-top">
      <span class="modal-fname" id="mTitle">—</span>
      <button class="modal-close" id="mClose">
        <svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="modal-body" id="mBody"></div>
  </div>
</div>

<script>
(function(){
'use strict';

/* ── CONFIG — replace these with Kotlin string interpolation ── */
const DEVICE_NAME = '$escapedName';
const NEEDS_AUTH  = $needsAuth;

/* ── DOM refs ── */
const authScreen = document.getElementById('authScreen');
const mainApp    = document.getElementById('mainApp');

/* ── BOOT ── */
if (NEEDS_AUTH) {
  bootAuth();
} else {
  authScreen.classList.add('hidden');
  mainApp.classList.add('show');
  bootApp();
}

/* ══════════════ AUTH ══════════════ */
function bootAuth() {
  document.getElementById('authDeviceName').textContent = DEVICE_NAME;

  const inputs  = document.querySelectorAll('#pinRow input');
  const btn     = document.getElementById('authBtn');
  const errEl   = document.getElementById('authErr');

  inputs.forEach((inp, i) => {
    inp.addEventListener('input', () => {
      inp.value = inp.value.replace(/\D/g,'');
      if (inp.value && i < inputs.length - 1) inputs[i+1].focus();
      btn.disabled = getPIN().length !== 4;
    });
    inp.addEventListener('keydown', e => {
      if (e.key === 'Backspace' && !inp.value && i > 0) inputs[i-1].focus();
      if (e.key === 'Enter') tryAuth();
    });
    inp.addEventListener('paste', e => {
      e.preventDefault();
      const p = (e.clipboardData||window.clipboardData).getData('text').replace(/\D/g,'');
      [...p].slice(0,4).forEach((c,j) => { inputs[j].value = c; });
      const last = Math.min(p.length, 4) - 1;
      if (last >= 0) inputs[last].focus();
      btn.disabled = getPIN().length !== 4;
    });
  });

  inputs[0].focus();
  btn.addEventListener('click', tryAuth);

  function getPIN() { return [...inputs].map(i => i.value).join(''); }

  function tryAuth() {
    const pin = getPIN();
    if (pin.length !== 4) return;
    btn.disabled = true; btn.textContent = 'Verifying…';
    errEl.textContent = '';
    fetch('/api/auth', {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({pin})
    })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        authScreen.classList.add('hidden');
        mainApp.classList.add('show');
        bootApp();
      } else {
        errEl.textContent = 'Wrong PIN — try again';
        inputs.forEach(i => { i.value=''; i.classList.add('err'); });
        setTimeout(() => inputs.forEach(i => i.classList.remove('err')), 500);
        inputs[0].focus();
        btn.disabled = true; btn.textContent = 'Unlock';
      }
    })
    .catch(() => {
      errEl.textContent = 'Connection error';
      btn.disabled = false; btn.textContent = 'Unlock';
    });
  }
}

/* ══════════════ APP ══════════════ */
function bootApp() {
  document.getElementById('topDevice').textContent = DEVICE_NAME;

  /* ── theme ── */
  const themeBtn = document.getElementById('themeBtn');
  const themeIco = document.getElementById('themeIco');
  const authLogo = document.getElementById('authLogo');
  const navLogo  = document.getElementById('navLogo');
  const SUN  = '<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>';
  const MOON = '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>';
  let dark = localStorage.getItem('ls-theme') === 'dark';
  applyTheme(dark);

  function applyTheme(isDark) {
    dark = isDark;
    if (isDark) {
      document.documentElement.style.setProperty('--bg','#0a0a0f');
      document.documentElement.style.setProperty('--bg2','#111118');
      document.documentElement.style.setProperty('--bg3','#1a1a24');
      document.documentElement.style.setProperty('--bg4','#22222f');
      document.documentElement.style.setProperty('--txt','#f0f0f8');
      document.documentElement.style.setProperty('--txt2','#9090a8');
      document.documentElement.style.setProperty('--txt3','#5a5a70');
      document.documentElement.style.setProperty('--line','rgba(255,255,255,0.07)');
      document.documentElement.style.setProperty('--line2','rgba(255,255,255,0.12)');
      document.documentElement.style.setProperty('--topbar-bg','rgba(10,10,15,0.85)');
      if(themeIco) themeIco.innerHTML = SUN;
      if(authLogo) authLogo.src = '/logo-dark.png';
      if(navLogo) navLogo.src = '/logo-dark.png';
    } else {
      document.documentElement.style.setProperty('--bg','#f5f5f8');
      document.documentElement.style.setProperty('--bg2','#ffffff');
      document.documentElement.style.setProperty('--bg3','#ebebf0');
      document.documentElement.style.setProperty('--bg4','#e0e0e8');
      document.documentElement.style.setProperty('--txt','#0a0a14');
      document.documentElement.style.setProperty('--txt2','#5a5a70');
      document.documentElement.style.setProperty('--txt3','#9090a8');
      document.documentElement.style.setProperty('--line','rgba(0,0,0,0.08)');
      document.documentElement.style.setProperty('--line2','rgba(0,0,0,0.14)');
      document.documentElement.style.setProperty('--topbar-bg','rgba(245,245,248,0.85)');
      if(themeIco) themeIco.innerHTML = MOON;
      if(authLogo) authLogo.src = '/logo.png';
      if(navLogo) navLogo.src = '/logo.png';
    }
    localStorage.setItem('ls-theme', isDark ? 'dark' : 'light');
  }
  if(themeBtn) themeBtn.addEventListener('click', () => applyTheme(!dark));

  /* prefer light theme on first visit */
  if (!localStorage.getItem('ls-theme')) {
    localStorage.setItem('ls-theme', 'light');
  }

  /* ── state ── */
  let allFiles = [], cat = 'all', q = '', sort = 'name', gridView = true;
  let lastFilesStr = '';
  const sel = new Set();

  /* ── refs ── */
  const grid    = document.getElementById('grid');
  const searchI = document.getElementById('searchInp');
  const chips   = document.getElementById('chips');
  const sortSel = document.getElementById('sortSel');
  const viewBtn = document.getElementById('viewBtn');
  const viewIco = document.getElementById('viewIco');
  const fab     = document.getElementById('fab');
  const fabCnt  = document.getElementById('fabCount');
  const fabClr  = document.getElementById('fabClr');
  const fabDl   = document.getElementById('fabDl');
  const overlay = document.getElementById('overlay');
  const mBody   = document.getElementById('mBody');
  const mTitle  = document.getElementById('mTitle');
  const mClose  = document.getElementById('mClose');

  const GRID_ICO = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>';
  const LIST_ICO = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>';

  /* ── events ── */
  searchI.addEventListener('input', () => { q = searchI.value.toLowerCase(); render(); });
  sortSel.addEventListener('change', () => { sort = sortSel.value; render(); });

  chips.addEventListener('click', e => {
    const c = e.target.closest('.chip');
    if (!c) return;
    document.querySelectorAll('.chip').forEach(x => x.classList.remove('on'));
    c.classList.add('on');
    cat = c.dataset.cat;
    render();
  });

  viewBtn.addEventListener('click', () => {
    gridView = !gridView;
    grid.classList.toggle('list', !gridView);
    viewIco.parentElement.innerHTML = gridView ? LIST_ICO : GRID_ICO;
  });

  fabClr.addEventListener('click', () => { sel.clear(); render(); updateFab(); });
  fabDl.addEventListener('click', () => {
    if (!sel.size) return;
    window.location.href = '/api/download-zip?ids=' + [...sel].join(',');
    sel.clear(); render(); updateFab();
  });

  mClose.addEventListener('click', closeModal);
  overlay.addEventListener('click', e => { if (e.target === overlay) closeModal(); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });

  /* ── fetch ── */
  load();
  setInterval(load, 5000);

  function load() {
    fetch('/api/files')
      .then(r => {
        if (r.status === 401) { location.reload(); return; }
        return r.json();
      })
      .then(d => {
        if (!d) return;
        const newStr = JSON.stringify(d.files || []);
        if (newStr !== lastFilesStr) {
          allFiles = d.files || [];
          lastFilesStr = newStr;
          render();
        }
        updateStats(d);
      })
      .catch(() => {
        grid.innerHTML = '<div class="state">' +
          '<svg viewBox="0 0 24 24"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>' +
          '<div class="state-t">Connection lost</div>' +
          '<div class="state-d">Cannot reach the phone. Make sure you\'re on the same WiFi.</div>' +
        '</div>';
      });
  }

  function updateStats(d) {
    document.getElementById('statFiles').textContent   = d.count ?? allFiles.length;
    document.getElementById('statDevices').textContent = d.connectedDevices ?? '—';
    document.getElementById('statPin').textContent     = d.pinProtected ? 'On' : 'Off';
  }

  /* ── render ── */
  function render() {
    let files = allFiles;
    if (cat !== 'all') files = files.filter(f => f.category === cat);
    if (q) files = files.filter(f => f.name.toLowerCase().includes(q));

    files = [...files].sort((a,b) => {
      if (sort === 'size') return b.size - a.size;
      if (sort === 'date') return b.lastModified - a.lastModified;
      return a.name.localeCompare(b.name);
    });

    if (!files.length) {
      grid.innerHTML = '<div class="state">' +
        '<svg viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>' +
        '<div class="state-t">No files found</div>' +
        '<div class="state-d">Try a different filter or search term</div>' +
      '</div>';
      return;
    }

    const STREAMABLE = new Set(['mp4','webm','mov','ogg','mp3','m4a','wav','aac','flac','jpg','jpeg','png','gif','webp']);

    grid.innerHTML = files.map(f => {
      const ext    = f.name.split('.').pop().toLowerCase();
      const canPl  = f.isStreamable && STREAMABLE.has(ext);
      const isImg  = ['jpg','jpeg','png','gif','webp','svg'].includes(ext);
      const isSel  = sel.has(f.id);
      const cls    = iconCls(f.typeIcon);

      const thumb  = (f.typeIcon === 'image' || f.typeIcon === 'video')
        ? '<img src="/api/thumbnail/' + f.id + '" alt="" loading="lazy" onerror="this.style.display=\'none\'">'
        : (f.typeIcon === 'android' ? '<img src="/api/icon/' + f.id + '" alt="" loading="lazy" onerror="this.style.display=\'none\'">' : iconSvg(f.typeIcon));

      const playBtn = canPl
        ? '<button class="faction play" title="' + (isImg?'View':'Play') + '" onclick="openMedia(' + f.id + ',\'' + attr(f.name) + '\',\'' + f.mimeType + '\');event.stopPropagation()">' +
             (isImg
               ? '<svg viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
               : '<svg viewBox="0 0 24 24"><polygon points="5 3 19 12 5 21 5 3"/></svg>') +
           '</button>' : '';

      return '<div class="fcard' + (isSel?' sel':'') + '" onclick="toggleSel(event,' + f.id + ',' + canPl + ',\'' + attr(f.name) + '\',\'' + f.mimeType + '\')">' +
        '<div class="fcheck" onclick="event.stopPropagation();toggleSel(event,' + f.id + ',false,\'\',\'\')">' +
          '<svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>' +
        '</div>' +
        '<div class="ficon ' + cls + '">' + thumb + '</div>' +
        '<div class="finfo">' +
          '<div class="fname" title="' + esc(f.name) + '">' + esc(f.name) + '</div>' +
          '<div class="fmeta">' + f.formattedSize + '</div>' +
        '</div>' +
        '<div class="factions" onclick="event.stopPropagation()">' +
          playBtn +
          '<a class="faction" title="Download" href="/download/' + f.id + '" download="' + esc(f.name) + '">' +
            '<svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>' +
          '</a>' +
        '</div>' +
      '</div>';
    }).join('');

    updateFab();
  }

  function updateFab() {
    if (sel.size > 0) {
      fab.classList.add('show');
      fabCnt.textContent = sel.size;
    } else {
      fab.classList.remove('show');
    }
  }

  /* ── exposed globals ── */
  window.toggleSel = function(e, id, canPl, name, mime) {
    if (sel.size > 0 || !canPl) {
      sel.has(id) ? sel.delete(id) : sel.add(id);
      render();
    } else if (canPl) {
      openMedia(id, name, mime);
    }
  };

  window.openMedia = function(id, name, mime) {
    mTitle.textContent = name;
    const url = '/stream/' + id;
    if (mime.startsWith('video/')) {
      mBody.innerHTML = '<video controls autoplay><source src="' + url + '" type="' + mime + '"></video>';
    } else if (mime.startsWith('audio/')) {
      mBody.innerHTML = '<audio controls autoplay style="width:100%"><source src="' + url + '" type="' + mime + '"></audio>';
    } else {
      mBody.innerHTML = '<img src="' + url + '" alt="' + esc(name) + '">';
    }
    overlay.classList.add('open');
  };

  function closeModal() {
    overlay.classList.remove('open');
    const m = mBody.querySelector('video,audio');
    if (m) { m.pause(); m.src = ''; }
    mBody.innerHTML = '';
  }

  /* ── helpers ── */
  function iconCls(t) {
    return t === 'video' ? 'vid' : t === 'image' ? 'img' : t === 'audio' ? 'aud' : 'doc';
  }

  function iconSvg(t) {
    const icons = {
      video: '<svg viewBox="0 0 24 24"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>',
      image: '<svg viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>',
      audio: '<svg viewBox="0 0 24 24"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>',
      document: '<svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
    };
    return icons[t] || icons.document;
  }

  function esc(s) {
    const d = document.createElement('div'); d.textContent = s; return d.innerHTML;
  }

  function attr(s) {
    return s.replace(/'/g, "\\'").replace(/"/g, '&quot;');
  }
}

})();
</script>
</body>
</html>
        """.trimIndent()
    }
}
