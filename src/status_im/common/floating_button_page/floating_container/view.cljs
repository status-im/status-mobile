(ns status-im.common.floating-button-page.floating-container.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.floating-container.style :as style]))

(defn- blur-container
  [child shell-overlay?]
  (let [theme (quo.theme/use-theme)]
    [quo/blur
     {:blur-amount   20
      :blur-type     :transparent
      :overlay-color :transparent}
     [rn/view {:style (style/blur-inner-container theme shell-overlay?)}
      child]]))

(defn view
  [{:keys [on-layout keyboard-shown? blur? shell-overlay?]} child]
  [rn/view
   {:style     (style/content-container blur? keyboard-shown?)
    :on-layout on-layout}
   (if blur?
     [blur-container child shell-overlay?]
     child)])
