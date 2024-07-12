(ns status-im.common.floating-button-page.floating-container.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.floating-container.style :as style]))

(defn- blur-container
  [child]
  (let [theme (quo.theme/use-theme)]
    [quo/blur
     {:blur-amount        52
      :blur-radius        20
      :blur-type          :transparent
      :blur-overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)}
     [rn/view {:style style/blur-inner-container}
      child]]))

(defn view
  [{:keys [on-layout keyboard-shown? blur?]} child]
  [rn/view
   {:style     (style/content-container blur? keyboard-shown?)
    :on-layout on-layout}
   (if blur?
     [blur-container child]
     child)])
