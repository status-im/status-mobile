(ns status-im.common.floating-button-page.floating-container.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.floating-container.style :as style]))

(defn- blur-container
  [child]
  (let [theme (quo.theme/use-theme)]
    [quo/blur
     {:blur-amount 12
      :blur-radius 12
      :blur-type   theme}
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
