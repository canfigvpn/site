(() => {
  "use strict";
  if (window.__EF3_PWA_INSTALL_READY__) return;
  window.__EF3_PWA_INSTALL_READY__ = true;

  const button = document.querySelector("[data-ef3-install]");
  if (!button) return;
  const originalLabel = button.textContent.trim() || "نصب اپ EF3";
  let installPrompt = null;

  const isIOS = () =>
    /iphone|ipad|ipod/i.test(navigator.userAgent) ||
    (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);

  const isInstalled = () =>
    window.matchMedia("(display-mode: standalone)").matches ||
    window.navigator.standalone === true;

  const notify = (message) => {
    let el = document.querySelector("[data-ef3-install-notice]");
    if (!el) {
      el = document.createElement("div");
      el.dataset.ef3InstallNotice = "";
      el.setAttribute("role", "status");
      Object.assign(el.style, {
        position: "fixed", zIndex: "2147483647", insetInline: "16px",
        bottom: "20px", maxWidth: "440px", marginInline: "auto",
        padding: "14px 16px", borderRadius: "14px", background: "#111827",
        color: "#fff", font: "14px/1.9 system-ui,sans-serif",
        boxShadow: "0 8px 30px #0005", textAlign: "right"
      });
      document.body.appendChild(el);
    }
    el.textContent = message;
    clearTimeout(el.__timer);
    el.__timer = setTimeout(() => el.remove(), 8500);
  };

  const refresh = () => {
    if (isInstalled()) {
      button.textContent = "اپ نصب شده است";
      button.disabled = true;
      button.setAttribute("aria-label", "اپ EF3 نصب شده است");
    } else {
      button.textContent = originalLabel;
      button.disabled = false;
    }
  };

  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    installPrompt = event;
  });

  window.addEventListener("appinstalled", () => {
    installPrompt = null;
    notify("اپ EF3 با موفقیت نصب شد.");
    refresh();
  });

  button.addEventListener("click", async () => {
    if (isInstalled()) {
      notify("اپ EF3 از قبل نصب شده است.");
      return;
    }
    if (installPrompt) {
      installPrompt.prompt();
      const choice = await installPrompt.userChoice.catch(() => null);
      installPrompt = null;
      if (choice?.outcome === "accepted") notify("درخواست نصب تأیید شد.");
      refresh();
      return;
    }
    if (isIOS()) {
      notify('برای نصب در آیفون، سایت را در Safari باز کنید، Share را بزنید و «Add to Home Screen» را انتخاب کنید.');
    } else {
      notify('اگر پنجره نصب باز نشد، از منوی مرورگر «Install app» یا «Add to Home screen» را انتخاب کنید. سایت باید HTTPS و PWA معتبر داشته باشد.');
    }
  });

  if ("serviceWorker" in navigator && location.protocol === "https:") {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/sw.js", { scope: "/" })
        .catch((error) => console.error("EF3 service worker registration failed:", error));
    });
  }
  refresh();
})();