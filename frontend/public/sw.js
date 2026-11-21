// Service worker di GymTracker: serve solo a ricevere le notifiche push (fine recupero) a schermo spento.
self.addEventListener('install', () => self.skipWaiting())
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()))

// Ogni push DEVE mostrare una notifica (su iOS, altrimenti Safari revoca la sottoscrizione)
self.addEventListener('push', event => {
  let d = {}
  try { d = event.data ? event.data.json() : {} } catch { /* payload non valido: si usano i testi predefiniti */ }
  event.waitUntil(self.registration.showNotification(d.titolo || 'Recupero terminato', {
    body: d.corpo || 'Puoi iniziare la serie successiva.',
    tag: 'recupero',      // sostituisce eventuali notifiche di recupero già presenti
    renotify: true,
    icon: '/icon-192.png',
    badge: '/icon-192.png',
    vibrate: [400, 150, 400, 150, 700]
  }))
})

self.addEventListener('notificationclick', event => {
  event.notification.close()
  event.waitUntil((async () => {
    const finestre = await self.clients.matchAll({ type: 'window', includeUncontrolled: true })
    for (const c of finestre) { if ('focus' in c) return c.focus() }
    return self.clients.openWindow('/')
  })())
})
