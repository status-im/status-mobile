(ns status-im2.common.floating-button-page.floating-container.view
  (:require [quo.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [status-im2.common.floating-button-page.floating-container.style :as style]))

(defn- blur-container
  [child theme]
  [blur/view
   {:blur-amount 12
    :blur-radius 12
    :blur-type   (quo.theme/theme-value :light :dark theme)}
   [rn/view {:style style/blur-inner-container}
    child]])

(defn view-internal
  [{:keys [theme on-layout keyboard-shown? blur?]} child]
  [rn/view
   {:style     (style/content-container blur? keyboard-shown?)
    :on-layout on-layout}
   (if blur?
     [blur-container child theme]
     child)])

(def view (quo.theme/with-theme view-internal))
