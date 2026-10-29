const CACHE_NAME = "ef3-pwa-v2";
const APP_SHELL = ["/", "/manifest.webmanifest", "/pwa/icon.svg"];

const offlinePage = () => new Response(`<!doctype html>
<html lang="fa" dir="rtl"><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="theme-color" content="#080b12">
<title>آفلاین | EF3</title>
<style>
*{box-sizing:border-box}body{margin:0;min-height:100vh;display:grid;place-items:center;padding:24px;background:#080b12;color:#f8fafc;font-family:system-ui,sans-serif;text-align:center}
main{width:min(100%,420px);padding:28px;border:1px solid #ffffff18;border-radius:24px;background:#111827}
h1{font-size:22px;margin:0 0 10px}p{color:#cbd5e1;line-height:1.9;margin:0 0 22px}
button{border:0;border-radius:12px;background:#d4af68;color:#111827;padding:12px 20px;font-weight:700;font-size:15px;cursor:pointer}
</style><main><h1>اتصال اینترنت برقرار نیست</h1>
<p>صفحه‌ای که می‌خواهی باز کنی در حافظه آفلاین موجود نیست. بعد از اتصال اینترنت دوباره تلاش کن.</p>
<button onclick="location.reload()">تلاش دوباره</button></main></html>`, {
  status: 200,
  headers: { "Content-Type": "text/html; charset=utf-8", "Cache-Control": "no-store" }
});

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(APP_SHELL).catch(() => undefined))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", event => {
  const request = event.request;
  const url = new URL(request.url);
  if (request.method !== "GET" || url.origin !== self.location.origin) return;

  if (request.mode === "navigate") {
    event.respondWith((async () => {
      try {
        const response = await fetch(request);
        if (response && response.ok) {
          const cache = await caches.open(CACHE_NAME);
          cache.put(request, response.clone()).catch(() => undefined);
        }
        return response;
      } catch (_) {
        return (await caches.match(request)) || (await caches.match("/")) || offlinePage();
      }
    })());
    return;
  }

  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;
    try {
      const response = await fetch(request);
      if (response && response.ok) {
        const cache = await caches.open(CACHE_NAME);
        cache.put(request, response.clone()).catch(() => undefined);
      }
      return response;
    } catch (_) {
      return Response.error();
    }
  })());
});