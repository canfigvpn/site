(() => {
  "use strict";

  if (window.__EF3_PWA_INSTALL_READY__) return;
  window.__EF3_PWA_INSTALL_READY__ = true;

  const installButton = document.querySelector("[data-ef3-install]");
  if (!installButton) return;

  let deferredPrompt = null;
  const defaultLabel = installButton.textContent.trim() || "نصب اپ EF3";

  const isIOS = () =>
    /iphone|ipad|ipod/i.test(navigator.userAgent) ||
    (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);

  const isInStandaloneMode = () =>
    window.matchMedia("(display-mode: standalone)").matches ||
    window.navigator.standalone === true;

  const showMessage = (message) => {
    let notice = document.querySelector("[data-ef3-install-notice]");
    if (!notice) {
      notice = document.createElement("div");
      notice.setAttribute("data-ef3-install-notice", "");
      notice.setAttribute("role", "status");
      Object.assign(notice.style, {
        position: "fixed",
        zIndex: "2147483647",
        insetInline: "16px",
        bottom: "20px",
        maxWidth: "440px",
        marginInline: "auto",
        padding: "14px 16px",
        borderRadius: "14px",
        background: "#111827",
        color: "#fff",
        font: "14px/1.9 system-ui, sans-serif",
        boxShadow: "0 8px 30px #0005",
        textAlign: "right"
      });
      document.body.appendChild(notice);
    }
    notice.textContent = message;
    clearTimeout(notice.__ef3Timer);
    notice.__ef3Timer = setTimeout(() => notice.remove(), 8000);
  };

  const updateButton = () => {
    if (isInStandaloneMode()) {
      installButton.textContent = "اپ نصب شده است";
      installButton.disabled = true;
      installButton.setAttribute("aria-label", "اپ EF3 نصب شده است");
    } else {
      installButton.textContent = defaultLabel;
      installButton.disabled = false;
    }
  };

  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    deferredPrompt = event;
    updateButton();
  });

  window.addEventListener("appinstalled", () => {
    deferredPrompt = null;
    showMessage("اپ EF3 با موفقیت نصب شد.");
    updateButton();
  });

  installButton.addEventListener("click", async () => {
    if (isInStandaloneMode()) {
      showMessage("اپ EF3 از قبل نصب شده است.");
      return;
    }

    if (deferredPrompt) {
      deferredPrompt.prompt();
      const result = await deferredPrompt.userChoice.catch(() => null);
      deferredPrompt = null;
      if (result && result.outcome === "accepted") {
        showMessage("درخواست نصب تأیید شد.");
      }
      updateButton();
      return;
    }

    if (isIOS()) {
      showMessage('برای نصب در آیفون: در Safari دکمه Share را بزنید و سپس «Add to Home Screen» را انتخاب کنید.');
      return;
    }

    showMessage('اگر گزینه نصب نمایش داده نمی‌شود، منوی مرورگر را باز کنید و «Install app» یا «Add to Home screen» را بزنید. سایت باید HTTPS و PWA معتبر داشته باشد.');
  });

  if ("serviceWorker" in navigator && location.protocol === "https:") {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/pwa/sw.js", { scope: "/" })
        .catch((error) => console.error("EF3 service worker registration failed:", error));
    });
  }

  updateButton();
})();