(ns status-im.common.pdf-viewer.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.pdf-viewer :as pdf-viewer]
    [utils.re-frame :as rf]))

(defn- on-close
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [uri pdf-viewer-props]} (rf/sub [:get-screen-params])]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            on-close
       :accessibility-label :pdf-viewer-nav}]
     [pdf-viewer/view
      (merge {:source {:uri uri}
              :style  {:flex 1}}
             pdf-viewer-props)]]))
