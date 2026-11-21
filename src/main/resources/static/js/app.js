(function () {
  'use strict';

  // mobile nav
  var toggle = document.querySelector('[data-nav-toggle]');
  var links = document.querySelector('[data-nav-links]');
  if (toggle && links) toggle.addEventListener('click', function () { links.classList.toggle('open'); });

  // gallery
  document.querySelectorAll('[data-gallery]').forEach(function (g) {
    var main = g.querySelector('[data-main]');
    g.querySelectorAll('[data-thumb]').forEach(function (t) {
      t.addEventListener('click', function () {
        main.src = t.dataset.full;
        g.querySelectorAll('[data-thumb]').forEach(function (o) { o.classList.remove('on'); });
        t.classList.add('on');
      });
    });
  });

  // booking form: casual vs permanent + live estimate
  var bf = document.querySelector('[data-booking-form]');
  if (bf) {
    var rate = parseFloat(bf.dataset.rate || '0');
    var casual = bf.querySelectorAll('[data-casual]');
    var perm = bf.querySelectorAll('[data-permanent]');
    var qty = bf.querySelector('[name=quantity]');
    var out = bf.querySelector('[data-estimate]');
    var fmt = function (n) { return 'GH₵ ' + n.toLocaleString('en-GH', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); };
    var refresh = function () {
      var typeEl = bf.querySelector('[name=type]:checked');
      var isPerm = typeEl && typeEl.value === 'PERMANENT';
      casual.forEach(function (e) { e.classList.toggle('hidden', isPerm); });
      perm.forEach(function (e) { e.classList.toggle('hidden', !isPerm); });
      if (out && qty) out.textContent = fmt(rate * Math.max(1, parseInt(qty.value || '1', 10)));
    };
    bf.addEventListener('input', refresh);
    bf.addEventListener('change', refresh);
    refresh();
  }

  // worker profile form: category-specific hints for the "details" box
  var cat = document.querySelector('[data-category-select]');
  var details = document.querySelector('[data-details]');
  var hint = document.querySelector('[data-details-hint]');
  if (cat && details) {
    var hints = {};
    try { hints = JSON.parse(cat.dataset.hints || '{}'); } catch (e) { /* ignore */ }
    var apply = function () {
      var h = hints[cat.value] || hints.DEFAULT || '';
      details.placeholder = h;
      if (hint) hint.textContent = h;
    };
    cat.addEventListener('change', apply);
    apply();
  }

  // registration: show category only for workers
  document.querySelectorAll('[data-role-radio]').forEach(function (r) {
    r.addEventListener('change', function () {
      var box = document.querySelector('[data-worker-only]');
      if (box) box.classList.toggle('hidden', r.value !== 'WORKER');
    });
  });
})();
