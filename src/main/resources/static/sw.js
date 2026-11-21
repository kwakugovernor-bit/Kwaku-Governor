/* Service worker: caches the app shell (CSS, JS, logo) and shows an offline page when the network is down.
   Signed-in pages are never stored, so private data does not stay on the device. */
const VERSION = 'ahh-v1';
const SHELL = ['/offline', '/css/app.css', '/js/app.js', '/images/logo.png', '/images/icon-192.png'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(VERSION).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) => Promise.all(keys.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()));
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  // pages: network only, offline page as fallback
  if (req.mode === 'navigate') {
    e.respondWith(fetch(req).catch(() => caches.match('/offline')));
    return;
  }
  // static assets: cache first, refresh in background
  if (/^\/(css|js|images)\//.test(url.pathname)) {
    e.respondWith(caches.match(req).then((hit) => {
      const net = fetch(req).then((res) => {
        if (res.ok) caches.open(VERSION).then((c) => c.put(req, res.clone()));
        return res;
      }).catch(() => hit);
      return hit || net;
    }));
  }
});
