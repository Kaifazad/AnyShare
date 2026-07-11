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
  --bg:       #FAFAFF;
  --bg2:      #FFFFFF;
  --bg3:      #F0F0FA;
  --bg4:      #E4E4EE;
  --line:     rgba(0,0,0,0.06);
  --line2:    rgba(0,0,0,0.12);
  --txt:      #1A1A24;
  --txt2:     #4A4A5A;
  --txt3:     #7A7A8A;
  --accent:   #005BFF;
  --accent2:  #4B90FF;
  --green:    #008744;
  --pink:     #D81B60;
  --amber:    #FF8F00;
  --blue:     #005BFF;
  --red:      #D32F2F;
  --r4:       4px;
  --r8:       8px;
  --r12:      12px;
  --r16:      16px;
  --r24:      24px;
  --r28:      28px;
  --r999:     999px;
  --font:     'DM Sans', sans-serif;
  --mono:     'Space Mono', monospace;
  --topbar-bg: rgba(250,250,255,0.85);
  --shadow:   0 4px 12px rgba(0,0,0,0.05);
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
  width:100%;height:56px;
  padding:0 16px 0 46px;
  background:var(--bg2);
  border:1px solid transparent;
  border-radius:var(--r28);
  box-shadow:var(--shadow);
  color:var(--txt);
  font-size:16px;font-family:var(--font);
  outline:none;
  transition:border-color .2s, box-shadow .2s;
}
.search-wrap input::placeholder{color:var(--txt3)}
.search-wrap input:focus{
  background:var(--bg2);
  border-color:var(--accent);
  box-shadow:0 0 0 4px rgba(0,91,255,0.15);
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
  border:1px solid transparent;
  box-shadow:var(--shadow);
  border-radius:var(--r24);
  padding:16px;
  display:flex;align-items:center;gap:14px;
  cursor:pointer;
  position:relative;
  transition:border-color .2s, background .2s, transform .2s, box-shadow .2s;
  overflow:hidden;
}
.fcard::after{
  content:'';
  position:absolute;inset:0;
  background:linear-gradient(135deg,rgba(0,91,255,0.04),transparent 60%);
  opacity:0;transition:opacity .2s;
  pointer-events:none;
}
.fcard:hover{
  border-color:rgba(0,91,255,0.35);
  box-shadow:0 8px 24px rgba(0,0,0,0.08);
  transform:translateY(-2px);
}
.fcard:hover::after{opacity:1}

.fcard.sel{
  border-color:var(--accent);
  background:rgba(0,91,255,0.08);
  box-shadow:0 0 0 2px var(--accent);
}

/* sel checkbox */
.fcheck{
  position:absolute;top:12px;left:12px;
  width:22px;height:22px;
  border-radius:4px;
  border:2px solid var(--line2);
  background:var(--bg3);
  display:flex;align-items:center;justify-content:center;
  opacity:0;
  transition:opacity .15s, background .15s, border-color .15s;
  z-index:2;
}
.fcard:hover .fcheck,.fcard.sel .fcheck{opacity:1}
.fcard.sel .fcheck{background:var(--accent);border-color:var(--accent)}
.fcheck svg{width:14px;height:14px;stroke:#fff;stroke-width:3;fill:none;stroke-linecap:round;stroke-linejoin:round;display:none}
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
  position:fixed;bottom:32px;left:50%;
  transform:translateX(-50%) translateY(80px);
  background:var(--bg2);
  border:none;
  border-radius:var(--r16);
  padding:12px 12px 12px 20px;
  display:flex;align-items:center;gap:12px;
  box-shadow:0 12px 32px rgba(0,0,0,0.15);
  z-index:90;
  transition:transform .35s cubic-bezier(.16,1,.3,1);
  white-space:nowrap;
}
.fab.show{transform:translateX(-50%) translateY(0)}
.fab-info{font-size:14px;font-weight:500;color:var(--txt2)}
.fab-count{
  font-family:var(--mono);
  font-size:14px;color:var(--txt);
  background:var(--bg3);
  padding:2px 8px; border-radius:var(--r8);
}
.fab-clr{
  font-size:14px;color:var(--txt2);
  background:none;border:none;cursor:pointer;
  font-family:var(--font);
  padding:6px 12px;border-radius:var(--r8);
  transition:color .15s, background .15s;
}
.fab-clr:hover{color:var(--txt);background:var(--bg3)}
.fab-dl{
  display:flex;align-items:center;gap:6px;
  height:40px;padding:0 20px;
  background:var(--accent);color:#fff;
  border:none;border-radius:var(--r12);
  font-size:14px;font-weight:600;font-family:var(--font);
  cursor:pointer;
  transition:opacity .15s, box-shadow .15s;
}
.fab-dl:hover{opacity:.9;box-shadow:0 4px 12px rgba(0,91,255,0.4)}
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

/* ── UPLOAD MODAL ── */
.upload-overlay{
  position:fixed;inset:0;
  background:rgba(0,0,0,0.6);
  backdrop-filter:blur(8px);
  -webkit-backdrop-filter:blur(8px);
  z-index:600;
  display:none;
  align-items:center;justify-content:center;
  padding:20px;
}
.upload-overlay.open{display:flex;animation:fadeIn .2s ease}

.upload-modal{
  background:var(--bg2);
  border:1px solid var(--line2);
  border-radius:var(--r16);
  width:100%;max-width:520px;
  padding:24px;
  animation:slideUp .3s cubic-bezier(.16,1,.3,1);
}
.upload-title{
  font-size:18px;font-weight:600;margin-bottom:4px;
}
.upload-sub{
  font-size:13px;color:var(--txt2);margin-bottom:20px;
}
.drop-zone{
  border:2px dashed var(--line2);
  border-radius:var(--r12);
  padding:40px 20px;
  text-align:center;
  cursor:pointer;
  transition:border-color .2s, background .2s;
}
.drop-zone:hover,.drop-zone.drag{
  border-color:var(--accent);
  background:rgba(108,99,255,0.06);
}
.drop-zone svg{
  width:36px;height:36px;stroke:var(--txt3);stroke-width:1.5;fill:none;
  stroke-linecap:round;stroke-linejoin:round;
  margin-bottom:12px;
}
.drop-zone.drag svg{stroke:var(--accent)}
.drop-hint{font-size:14px;color:var(--txt2);margin-bottom:4px}
.drop-hint b{color:var(--accent);cursor:pointer}
.drop-sub{font-size:12px;color:var(--txt3)}

.upload-list{
  margin-top:16px;max-height:200px;overflow-y:auto;
}
.upload-item{
  display:flex;align-items:center;gap:10px;
  padding:8px 12px;
  background:var(--bg3);
  border-radius:var(--r8);
  margin-bottom:6px;
  font-size:13px;
}
.upload-item-name{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.upload-item-size{color:var(--txt3);font-family:var(--mono);font-size:11px;flex-shrink:0}
.upload-item-remove{
  width:20px;height:20px;
  background:none;border:none;
  color:var(--txt3);cursor:pointer;
  display:flex;align-items:center;justify-content:center;
  border-radius:50%;
  transition:background .15s, color .15s;
}
.upload-item-remove:hover{background:var(--red);color:#fff}
.upload-item-remove svg{width:12px;height:12px;stroke:currentColor;stroke-width:2.5;fill:none}

.upload-actions{
  display:flex;gap:10px;margin-top:16px;justify-content:flex-end;
}
.upload-cancel{
  height:38px;padding:0 20px;
  background:var(--bg3);border:1px solid var(--line);
  border-radius:var(--r8);
  color:var(--txt2);font-size:13px;font-weight:500;
  font-family:var(--font);cursor:pointer;
  transition:background .15s;
}
.upload-cancel:hover{background:var(--bg4)}
.upload-send{
  height:38px;padding:0 20px;
  background:var(--accent);border:none;
  border-radius:var(--r8);
  color:#fff;font-size:13px;font-weight:600;
  font-family:var(--font);cursor:pointer;
  display:flex;align-items:center;gap:6px;
  transition:opacity .15s;
}
.upload-send:hover{opacity:.9}
.upload-send:disabled{opacity:.4;cursor:not-allowed}
.upload-send svg{width:14px;height:14px;stroke:#fff;stroke-width:2.5;fill:none;stroke-linecap:round;stroke-linejoin:round}

.upload-progress{
  margin-top:12px;
  height:4px;background:var(--bg3);border-radius:99px;overflow:hidden;
  display:none;
}
.upload-progress-bar{
  height:100%;background:var(--accent);border-radius:99px;
  width:0%;transition:width .3s;
}

.upload-result{
  margin-top:12px;padding:10px 14px;
  background:rgba(34,197,94,0.1);
  border:1px solid rgba(34,197,94,0.2);
  border-radius:var(--r8);
  color:var(--green);
  font-size:13px;font-weight:500;
  display:none;
}

.hidden{display:none!important}

/* ── CLIPBOARD SYNC ── */
.clip-fab {
  position:fixed; bottom:24px; left:24px; z-index:90;
  width:48px; height:48px;
  background:var(--accent); border:none; border-radius:50%;
  display:flex; align-items:center; justify-content:center;
  cursor:pointer; box-shadow:0 4px 16px rgba(108,99,255,0.4);
  transition:transform .2s, box-shadow .2s;
}
.clip-fab:hover { transform:scale(1.1); box-shadow:0 6px 20px rgba(108,99,255,0.5); }
.clip-fab svg { width:22px; height:22px; stroke:#fff; stroke-width:2; fill:none; stroke-linecap:round; stroke-linejoin:round; }
.clip-fab .clip-dot {
  position:absolute; top:2px; right:2px; width:10px; height:10px;
  border-radius:50%; background:var(--green); border:2px solid var(--bg);
  display:none;
}
.clip-fab .clip-dot.active { display:block; animation:pulse-dot 1.5s infinite; }
@keyframes pulse-dot { 0%,100%{opacity:1} 50%{opacity:.4} }

.clip-panel {
  position:fixed; bottom:82px; left:24px; z-index:91;
  width:340px; max-width:calc(100vw - 48px);
  background:var(--bg2); border:1px solid var(--line2);
  border-radius:var(--r16); box-shadow:0 8px 32px rgba(0,0,0,0.15);
  padding:20px; display:none;
  animation:fadeUp .3s ease both;
}
.clip-panel.open { display:block; }
.clip-panel-title {
  font-size:15px; font-weight:600; margin-bottom:4px;
  display:flex; align-items:center; gap:8px;
}
.clip-panel-title svg { width:16px; height:16px; stroke:var(--accent); stroke-width:2; fill:none; }
.clip-panel-sub { font-size:12px; color:var(--txt3); margin-bottom:14px; }

.clip-section { margin-bottom:14px; }
.clip-label {
  font-size:11px; font-weight:600; text-transform:uppercase;
  letter-spacing:1px; color:var(--txt3); margin-bottom:6px;
  font-family:var(--mono);
}
.clip-text {
  background:var(--bg3); border-radius:var(--r8);
  padding:10px 12px; font-size:13px; color:var(--txt);
  max-height:200px; overflow-y:auto; word-break:break-word;
  white-space:pre-wrap;
  min-height:36px; line-height:1.5;
  user-select:text; -webkit-user-select:text;
}
.clip-text.empty { color:var(--txt3); font-style:italic; }

.clip-input {
  width:100%; background:var(--bg3); border:1px solid var(--line);
  border-radius:var(--r8); padding:10px 12px;
  font-size:13px; color:var(--txt); font-family:var(--font);
  resize:vertical; min-height:60px; max-height:120px;
  outline:none; transition:border-color .2s;
}
.clip-input:focus { border-color:var(--accent); }
.clip-input::placeholder { color:var(--txt3); }

.clip-send-btn {
  margin-top:8px; width:100%; padding:8px 16px;
  background:var(--accent); border:none; border-radius:var(--r8);
  color:#fff; font-size:13px; font-weight:600;
  font-family:var(--font); cursor:pointer;
  display:flex; align-items:center; justify-content:center; gap:6px;
  transition:opacity .15s;
}
.clip-send-btn:hover { opacity:.9; }
.clip-send-btn svg { width:14px; height:14px; stroke:#fff; stroke-width:2.5; fill:none; stroke-linecap:round; stroke-linejoin:round; }

.clip-status {
  margin-top:8px; font-size:11px; color:var(--green);
  text-align:center; min-height:16px;
}
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

    <!-- search & upload -->
    <div style="display:flex; gap: 12px; margin-bottom: 24px;">
      <div class="search-wrap" style="flex: 1; margin-bottom: 0;">
        <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input type="text" id="searchInp" placeholder="Search files…" autocomplete="off">
      </div>
      <button class="upload-send" id="uploadBtn" style="height: 48px; border-radius: var(--r12); flex-shrink: 0;">
        <svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        Upload to Phone
      </button>
      <button id="clearAllBtn" style="height: 48px; border-radius: var(--r12); flex-shrink: 0; background: var(--bg3); color: var(--txt); border: 1px solid var(--line2); cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 0 16px; font-weight: 600; font-size: 14px;">
        <svg viewBox="0 0 24 24" style="width: 18px; height: 18px; stroke: var(--txt); stroke-width: 2; fill: none; stroke-linecap: round; stroke-linejoin: round;"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        Clear All
      </button>
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

<!-- ── CLIPBOARD SYNC ── -->
<button class="clip-fab" id="clipFab" data-tooltip="Clipboard Sync">
  <svg viewBox="0 0 24 24"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/></svg>
  <span class="clip-dot" id="clipDot"></span>
</button>
<div class="clip-panel" id="clipPanel">
  <div class="clip-panel-title">
    <svg viewBox="0 0 24 24"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/></svg>
    Clipboard Sync
  </div>
  <div class="clip-panel-sub">Sync clipboard between phone and laptop in real-time</div>

  <div class="clip-section">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
      <div class="clip-label" style="margin-bottom:0;">Phone Clipboard</div>
      <button id="phoneClipCopyBtn" style="background:none; border:none; cursor:pointer; color:var(--accent); font-size:11px; font-weight:600; font-family:var(--font); text-transform:uppercase; display:none; padding:4px;">COPY</button>
    </div>
    <div class="clip-text empty" id="phoneClip">Waiting for phone clipboard…</div>
  </div>

  <div class="clip-section" id="sharedTextSection" style="display:none;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
      <div class="clip-label" style="margin-bottom:0; color:var(--accent);">📲 Shared from Phone</div>
      <button id="sharedTextCopyBtn" style="background:none; border:none; cursor:pointer; color:var(--accent); font-size:11px; font-weight:600; font-family:var(--font); text-transform:uppercase; padding:4px;">COPY</button>
    </div>
    <div class="clip-text" id="sharedTextDisplay" style="background:rgba(0,91,255,0.06); border:1px solid rgba(0,91,255,0.15);"></div>
  </div>

  <div class="clip-section">
    <div class="clip-label">Send to Phone</div>
    <textarea class="clip-input" id="clipInput" placeholder="Type or paste text here to send to phone…"></textarea>
    <button class="clip-send-btn" id="clipSendBtn">
      <svg viewBox="0 0 24 24"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
      Send to Phone
    </button>
    <div class="clip-status" id="clipStatus"></div>
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

<!-- ── UPLOAD MODAL ── -->
<div class="upload-overlay" id="uploadOverlay">
  <div class="upload-modal">
    <div class="upload-title">Send files to phone</div>
    <div class="upload-sub">Drop files here or click to browse. Files will be saved to <b>Downloads/LocalShare</b> on the phone.</div>
    <div class="drop-zone" id="dropZone">
      <svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
      <div class="drop-hint">Drag & drop files or <b>browse</b></div>
      <div class="drop-sub">Any file type supported</div>
    </div>
    <input type="file" id="uploadFileInput" multiple style="display:none">
    <div class="upload-list" id="uploadList"></div>
    <div class="upload-progress" id="uploadProgress"><div class="upload-progress-bar" id="uploadProgressBar"></div></div>
    <div class="upload-result" id="uploadResult"></div>
    <div class="upload-actions">
      <button class="upload-cancel" id="uploadCancel">Cancel</button>
      <button class="upload-send" id="uploadSend" disabled>
        <svg viewBox="0 0 24 24"><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        Send to Phone
      </button>
    </div>
  </div>
</div>

<!-- ── CUSTOM CONFIRM MODAL ── -->
<div class="upload-overlay" id="confirmOverlay">
  <div class="upload-modal" style="max-width: 400px; text-align: center; padding: 32px 24px;">
    <svg viewBox="0 0 24 24" style="width: 48px; height: 48px; stroke: var(--red); stroke-width: 1.5; fill: none; stroke-linecap: round; stroke-linejoin: round; margin-bottom: 16px;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
    <div class="upload-title" id="confirmTitle" style="font-size: 20px; margin-bottom: 8px;">Are you sure?</div>
    <div class="upload-sub" id="confirmMessage" style="font-size: 14px; margin-bottom: 24px;">This action cannot be undone.</div>
    <div class="upload-actions" style="justify-content: center; gap: 12px; margin-top: 0;">
      <button class="upload-cancel" id="confirmCancel" style="flex: 1;">Cancel</button>
      <button class="upload-send" id="confirmOk" style="flex: 1; background: var(--red);">Yes, clear</button>
    </div>
  </div>
</div>

<script>
(function(){
'use strict';

/* ── CONFIG — replace these with Kotlin string interpolation ── */
const DEVICE_NAME = '$escapedName';
const NEEDS_AUTH  = $needsAuth;

/* ── helpers ── */
function esc(s) {
  const d = document.createElement('div'); d.textContent = s; return d.innerHTML;
}

function attr(s) {
  return s.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

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
      document.documentElement.style.setProperty('--bg','#121212');
      document.documentElement.style.setProperty('--bg2','#1E1E1E');
      document.documentElement.style.setProperty('--bg3','#282828');
      document.documentElement.style.setProperty('--bg4','#333333');
      document.documentElement.style.setProperty('--txt','#F0F0F8');
      document.documentElement.style.setProperty('--txt2','#A0A0A8');
      document.documentElement.style.setProperty('--txt3','#707078');
      document.documentElement.style.setProperty('--line','rgba(255,255,255,0.06)');
      document.documentElement.style.setProperty('--line2','rgba(255,255,255,0.12)');
      document.documentElement.style.setProperty('--topbar-bg','rgba(18,18,18,0.85)');
      if(themeIco) themeIco.innerHTML = SUN;
      if(authLogo) authLogo.src = '/logo-dark.png';
      if(navLogo) navLogo.src = '/logo-dark.png';
    } else {
      document.documentElement.style.setProperty('--bg','#FAFAFF');
      document.documentElement.style.setProperty('--bg2','#FFFFFF');
      document.documentElement.style.setProperty('--bg3','#F0F0FA');
      document.documentElement.style.setProperty('--bg4','#E4E4EE');
      document.documentElement.style.setProperty('--txt','#1A1A24');
      document.documentElement.style.setProperty('--txt2','#4A4A5A');
      document.documentElement.style.setProperty('--txt3','#7A7A8A');
      document.documentElement.style.setProperty('--line','rgba(0,0,0,0.06)');
      document.documentElement.style.setProperty('--line2','rgba(0,0,0,0.12)');
      document.documentElement.style.setProperty('--topbar-bg','rgba(250,250,255,0.85)');
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

    const STREAMABLE = new Set(['mp4','webm','mov','mkv','avi','m4v','ts','3gp','flv','wmv','ogg','mp3','m4a','wav','aac','flac','wma','jpg','jpeg','png','gif','webp','bmp','svg']);

    grid.innerHTML = files.map(f => {
      const ext    = f.name.split('.').pop().toLowerCase();
      const canPl  = f.isStreamable && STREAMABLE.has(ext);
      const isImg  = ['jpg','jpeg','png','gif','webp','svg'].includes(ext);
      const isSel  = sel.has(f.id);
      const cls    = iconCls(f.typeIcon);

      const thumb  = (f.typeIcon === 'image' || f.typeIcon === 'video')
        ? '<img src="/api/thumbnail/' + f.id + '" alt="" loading="lazy" onerror="this.style.display=\'none\'">'
        : (f.typeIcon === 'android' ? '<img src="/api/icon/' + f.id + '" alt="" loading="lazy" onerror="this.style.display=\'none\'">' : iconSvg(f.typeIcon));

      const isUnplayableVideo = (f.typeIcon === 'video' && !canPl);

      let playBtn = '';
      if (canPl) {
        playBtn = '<button class="faction play" data-tooltip="' + (isImg?'View':'Play') + '" onclick="openMedia(' + f.id + ',\'' + attr(f.name) + '\',\'' + f.mimeType + '\');event.stopPropagation()">' +
             (isImg
               ? '<svg viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
               : '<svg viewBox="0 0 24 24"><polygon points="5 3 19 12 5 21 5 3"/></svg>') +
           '</button>';
      } else if (isUnplayableVideo) {
        playBtn = '<div style="font-size: 11px; font-weight: 600; color: var(--txt3); text-transform: uppercase; margin-right: 4px; letter-spacing: 0.5px; display: flex; align-items: center;">Download only</div>';
      }

      return '<div class="fcard' + (isSel?' sel':'') + '" onclick="toggleSel(event,' + f.id + ',' + canPl + ',\'' + attr(f.name) + '\',\'' + f.mimeType + '\')">' +
        '<div class="fcheck" onclick="event.stopPropagation();toggleSel(event,' + f.id + ',false,\'\',\'\')">' +
          '<svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>' +
        '</div>' +
        '<div class="ficon ' + cls + '">' + thumb + '</div>' +
        '<div class="finfo">' +
          '<div class="fname" data-tooltip="' + esc(f.name) + '">' + esc(f.name) + '</div>' +
          '<div class="fmeta">' + f.formattedSize + '</div>' +
        '</div>' +
        '<div class="factions" onclick="event.stopPropagation()">' +
          playBtn +
          '<a class="faction" data-tooltip="Download" href="/download/' + f.id + '" download="' + esc(f.name) + '">' +
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
      mBody.innerHTML = '<video id="mediaPlayer" controls preload="metadata" playsinline></video>';
      const v = document.getElementById('mediaPlayer');
      v.src = url;
      v.play().catch(() => {});
    } else if (mime.startsWith('audio/')) {
      mBody.innerHTML = '<audio id="mediaPlayer" controls preload="metadata" style="width:100%"></audio>';
      const v = document.getElementById('mediaPlayer');
      v.src = url;
      v.play().catch(() => {});
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
  bootUpload();
}

/* ══════════════ UPLOAD ══════════════ */
function bootUpload() {
  const uploadBtn = document.getElementById('uploadBtn');
  const uploadOverlay = document.getElementById('uploadOverlay');
  const dropZone = document.getElementById('dropZone');
  const fileInput = document.getElementById('uploadFileInput');
  const uploadList = document.getElementById('uploadList');
  const uploadSend = document.getElementById('uploadSend');
  const uploadCancel = document.getElementById('uploadCancel');
  const uploadProgress = document.getElementById('uploadProgress');
  const uploadProgressBar = document.getElementById('uploadProgressBar');
  const uploadResult = document.getElementById('uploadResult');

  let pendingFiles = [];

  uploadBtn.addEventListener('click', () => {
    pendingFiles = [];
    renderUploadList();
    uploadProgress.style.display = 'none';
    uploadProgressBar.style.width = '0%';
    uploadResult.style.display = 'none';
    uploadSend.disabled = true;
    uploadOverlay.classList.add('open');
  });

  uploadCancel.addEventListener('click', () => {
    uploadOverlay.classList.remove('open');
  });

  uploadOverlay.addEventListener('click', (e) => {
    if (e.target === uploadOverlay) uploadOverlay.classList.remove('open');
  });

  dropZone.addEventListener('click', () => fileInput.click());

  dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('drag');
  });
  dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('drag');
  });
  dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('drag');
    addFiles(e.dataTransfer.files);
  });

  fileInput.addEventListener('change', () => {
    addFiles(fileInput.files);
    fileInput.value = '';
  });

  function addFiles(fileList) {
    for (let i = 0; i < fileList.length; i++) {
      pendingFiles.push(fileList[i]);
    }
    renderUploadList();
    uploadSend.disabled = pendingFiles.length === 0;
  }

  function formatSize(b) {
    if (b < 1024) return b + ' B';
    if (b < 1048576) return (b/1024).toFixed(1) + ' KB';
    return (b/1048576).toFixed(1) + ' MB';
  }

  function renderUploadList() {
    if (pendingFiles.length === 0) { uploadList.innerHTML = ''; return; }
    uploadList.innerHTML = pendingFiles.map((f, i) =>
      '<div class="upload-item">' +
        '<span class="upload-item-name">' + esc(f.name) + '</span>' +
        '<span class="upload-item-size">' + formatSize(f.size) + '</span>' +
        '<button class="upload-item-remove" data-idx="' + i + '">' +
          '<svg viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>' +
        '</button>' +
      '</div>'
    ).join('');

    uploadList.querySelectorAll('.upload-item-remove').forEach(btn => {
      btn.addEventListener('click', () => {
        pendingFiles.splice(parseInt(btn.dataset.idx), 1);
        renderUploadList();
        uploadSend.disabled = pendingFiles.length === 0;
      });
    });
  }

  function showConfirm(title, message) {
    return new Promise(resolve => {
      const overlay = document.getElementById('confirmOverlay');
      document.getElementById('confirmTitle').textContent = title;
      document.getElementById('confirmMessage').textContent = message;
      
      const onOk = () => {
        overlay.classList.remove('open');
        cleanup();
        resolve(true);
      };
      
      const onCancel = () => {
        overlay.classList.remove('open');
        cleanup();
        resolve(false);
      };
      
      const cleanup = () => {
        document.getElementById('confirmOk').removeEventListener('click', onOk);
        document.getElementById('confirmCancel').removeEventListener('click', onCancel);
      };
      
      document.getElementById('confirmOk').addEventListener('click', onOk);
      document.getElementById('confirmCancel').addEventListener('click', onCancel);
      
      overlay.classList.add('open');
    });
  }

  document.getElementById('clearAllBtn').addEventListener('click', async () => {
    const confirmed = await showConfirm('Clear All Shared Files?', 'This will stop sharing all files currently on the list. Are you sure you want to proceed?');
    if (!confirmed) return;
    try {
      await fetch('/api/files/clear', { method: 'POST' });
      load();
    } catch(e) {}
  });

  uploadSend.addEventListener('click', async () => {
    if (pendingFiles.length === 0) return;
    uploadSend.disabled = true;
    uploadProgress.style.display = 'block';
    uploadResult.style.display = 'none';

    let uploaded = 0;
    const total = pendingFiles.length;

    for (const file of pendingFiles) {
      const form = new FormData();
      form.append('file', file, file.name);
      form.append('filename', file.name);

      try {
        await new Promise((resolve, reject) => {
          const xhr = new XMLHttpRequest();
          xhr.open('POST', '/api/upload');
          xhr.upload.onprogress = (e) => {
            if (e.lengthComputable) {
              const filePct = e.loaded / e.total;
              const totalPct = ((uploaded + filePct) / total) * 100;
              uploadProgressBar.style.width = totalPct + '%';
            }
          };
          xhr.onload = () => {
            uploaded++;
            uploadProgressBar.style.width = ((uploaded / total) * 100) + '%';
            resolve();
          };
          xhr.onerror = () => reject(new Error('Network error'));
          xhr.send(form);
        });
      } catch(e) {
        console.error('Upload error:', e);
      }
    }

    uploadResult.textContent = uploaded + ' file(s) sent to phone successfully!';
    uploadResult.style.display = 'block';
    pendingFiles = [];
    renderUploadList();

    setTimeout(() => {
      uploadOverlay.classList.remove('open');
    }, 2000);
  });
}

})();

/* ══════════════ CLIPBOARD SYNC ══════════════ */
(function() {
  'use strict';
  const clipFab = document.getElementById('clipFab');
  const clipPanel = document.getElementById('clipPanel');
  const clipDot = document.getElementById('clipDot');
  const phoneClip = document.getElementById('phoneClip');
  const clipInput = document.getElementById('clipInput');
  const clipSendBtn = document.getElementById('clipSendBtn');
  const clipStatus = document.getElementById('clipStatus');

  let lastVersion = -1;
  let panelOpen = false;

  // Toggle panel
  clipFab.addEventListener('click', () => {
    panelOpen = !panelOpen;
    clipPanel.classList.toggle('open', panelOpen);
    if (panelOpen) clipDot.classList.remove('active');
  });

  // Close panel when clicking outside
  document.addEventListener('click', (e) => {
    if (panelOpen && !clipPanel.contains(e.target) && !clipFab.contains(e.target)) {
      panelOpen = false;
      clipPanel.classList.remove('open');
    }
  });

  // Helper: copy text to clipboard (works on HTTP, not just HTTPS)
  function copyToClipboard(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text);
    }
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.left = '-9999px';
    ta.style.top = '-9999px';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    try { document.execCommand('copy'); } catch(e) {}
    document.body.removeChild(ta);
    return Promise.resolve();
  }

  // Copy button for phone clipboard
  const phoneClipCopyBtn = document.getElementById('phoneClipCopyBtn');
  phoneClipCopyBtn.addEventListener('click', () => {
    copyToClipboard(phoneClip.textContent).then(() => {
      phoneClipCopyBtn.textContent = 'COPIED!';
      setTimeout(() => phoneClipCopyBtn.textContent = 'COPY', 2000);
    });
  });

  // Shared text elements
  const sharedTextSection = document.getElementById('sharedTextSection');
  const sharedTextDisplay = document.getElementById('sharedTextDisplay');
  const sharedTextCopyBtn = document.getElementById('sharedTextCopyBtn');

  sharedTextCopyBtn.addEventListener('click', () => {
    copyToClipboard(sharedTextDisplay.textContent).then(() => {
      sharedTextCopyBtn.textContent = 'COPIED!';
      setTimeout(() => sharedTextCopyBtn.textContent = 'COPY', 2000);
    });
  });

  // Send clipboard to phone
  clipSendBtn.addEventListener('click', async () => {
    const text = clipInput.value.trim();
    if (!text) return;
    clipSendBtn.disabled = true;
    try {
      const res = await fetch('/api/clipboard', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({text})
      });
      const data = await res.json();
      if (data.success) {
        clipStatus.textContent = 'Sent to phone!';
        clipStatus.style.color = 'var(--green)';
        clipInput.value = '';
        setTimeout(() => { clipStatus.textContent = ''; }, 3000);
      }
    } catch(e) {
      clipStatus.textContent = 'Failed to send';
      clipStatus.style.color = 'var(--red)';
    }
    clipSendBtn.disabled = false;
  });

  // Poll phone clipboard
  async function pollClipboard() {
    try {
      const res = await fetch('/api/clipboard');
      const data = await res.json();
      if (data.version !== lastVersion) {
        lastVersion = data.version;

        // Update system clipboard section
        if (data.text) {
          phoneClip.textContent = data.text;
          phoneClip.classList.remove('empty');
          phoneClipCopyBtn.style.display = 'block';
        } else {
          phoneClip.textContent = 'Phone clipboard is empty';
          phoneClip.classList.add('empty');
          phoneClipCopyBtn.style.display = 'none';
        }

        // Update shared text section
        if (data.sharedText) {
          sharedTextDisplay.textContent = data.sharedText;
          sharedTextSection.style.display = 'block';
          if (!panelOpen) {
            clipDot.classList.add('active');
          }
        } else {
          sharedTextSection.style.display = 'none';
        }
      }
    } catch(e) { /* ignore */ }
  }

  // Start polling every 3 seconds
  setInterval(pollClipboard, 3000);
  pollClipboard();
})();
</script>
</body>
</html>
        """.trimIndent()
    }
}
