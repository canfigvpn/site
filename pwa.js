/* EF3 PWA — merged installer, UI and registration.
   Keep this file, /sw.js, /manifest.webmanifest and /pwa/icon.svg on the SAME origin as the website. */
(() => {
  "use strict";
  if (window.__EF3_PWA_READY__) return;
  window.__EF3_PWA_READY__ = true;

  const ICON = "/pwa/icon.svg";
  const addHeadAssets = () => {
    if (!document.querySelector('link[rel="manifest"]')) {
      const link = document.createElement("link");
      link.rel = "manifest";
      link.href = "/manifest.webmanifest";
      document.head.appendChild(link);
    }
    const theme = document.querySelector('meta[name="theme-color"]') || document.createElement("meta");
    theme.name = "theme-color";
    theme.content = "#080b12";
    if (!theme.parentNode) document.head.appendChild(theme);
    if (!document.querySelector('link[rel="icon"]')) {
      const icon = document.createElement("link");
      icon.rel = "icon";
      icon.href = ICON;
      icon.type = "image/svg+xml";
      document.head.appendChild(icon);
    }
    const apple = document.querySelector('link[rel="apple-touch-icon"]') || document.createElement("link");
    apple.rel = "apple-touch-icon";
    apple.href = ICON;
    if (!apple.parentNode) document.head.appendChild(apple);
  };

  const installButton = () => {
    let button = document.querySelector("[data-ef3-install]");
    if (button) return button;
    button = document.createElement("button");
    button.type = "button";
    button.dataset.ef3Install = "";
    button.textContent = "نصب اپ";
    button.setAttribute("aria-label", "نصب اپ EF3");
    Object.assign(button.style, {
      position: "fixed", zIndex: "2147483000", insetInlineStart: "14px",
      bottom: "calc(14px + env(safe-area-inset-bottom, 0px))",
      border: "1px solid #d5b477", borderRadius: "999px",
      padding: "10px 15px", background: "#111827", color: "#fff",
      font: "600 13px/1.4 system-ui,sans-serif", boxShadow: "0 5px 20px #0003",
      cursor: "pointer"
    });
    document.body.appendChild(button);
    return button;
  };

  const isIOS = () =>
    /iphone|ipad|ipod/i.test(navigator.userAgent) ||
    (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
  const isInstalled = () =>
    window.matchMedia("(display-mode: standalone)").matches ||
    window.navigator.standalone === true;

  let deferredPrompt = null;
  let button;
  const notice = (message) => {
    let el = document.querySelector("[data-ef3-pwa-notice]");
    if (!el) {
      el = document.createElement("div");
      el.dataset.ef3PwaNotice = "";
      el.setAttribute("role", "status");
      Object.assign(el.style, {
        position: "fixed", zIndex: "2147483647", insetInline: "14px",
        bottom: "70px", maxWidth: "440px", marginInline: "auto",
        padding: "12px 15px", borderRadius: "13px", background: "#111827",
        color: "#fff", border: "1px solid #334155",
        font: "14px/1.9 system-ui,sans-serif", textAlign: "right",
        boxShadow: "0 8px 28px #0005"
      });
      document.body.appendChild(el);
    }
    el.textContent = message;
    clearTimeout(el.__ef3Timer);
    el.__ef3Timer = setTimeout(() => el.remove(), 8000);
  };

  const refreshButton = () => {
    if (!button) return;
    if (isInstalled()) {
      button.textContent = "اپ نصب شده";
      button.disabled = true;
      button.style.opacity = ".8";
    } else {
      button.textContent = "نصب اپ";
      button.disabled = false;
      button.style.opacity = "1";
    }
  };

  const start = () => {
    addHeadAssets();
    button = installButton();
    button.addEventListener("click", async () => {
      if (isInstalled()) {
        notice("اپ EF3 از قبل نصب شده است.");
      } else if (deferredPrompt) {
        deferredPrompt.prompt();
        try {
          const choice = await deferredPrompt.userChoice;
          notice(choice.outcome === "accepted" ? "درخواست نصب تأیید شد." : "نصب لغو شد.");
        } catch (_) {
          notice("فرایند نصب در مرورگر کامل نشد.");
        }
        deferredPrompt = null;
        refreshButton();
      } else if (isIOS()) {
        notice('برای نصب در آیفون، سایت را در Safari باز کن، Share را بزن و «Add to Home Screen» را انتخاب کن.');
      } else {
        notice('از منوی مرورگر گزینه «Install app» یا «Add to Home screen» را انتخاب کن. نصب به پشتیبانی مرورگر و تنظیم صحیح PWA نیاز دارد.');
      }
    });
    window.addEventListener("beforeinstallprompt", (event) => {
      event.preventDefault();
      deferredPrompt = event;
      refreshButton();
    });
    window.addEventListener("appinstalled", () => {
      deferredPrompt = null;
      notice("اپ EF3 با موفقیت نصب شد.");
      refreshButton();
    });
    window.matchMedia("(display-mode: standalone)").addEventListener?.("change", refreshButton);
    refreshButton();

    if ("serviceWorker" in navigator && location.protocol === "https:") {
      navigator.serviceWorker.register("/sw.js", { scope: "/" })
        .catch((error) => console.error("EF3 service worker registration failed:", error));
    }
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", start, { once: true });
  } else {
    start();
  }
})();