(ns status-im.common.pdf-viewer.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.pdf-viewer :as pdf-viewer]
    [status-im.common.events-helper :as events-helper]))

(defn view
  []
  (let [{:keys [uri pdf-viewer-props]} (quo.context/use-screen-params)]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            events-helper/navigate-back
       :accessibility-label :pdf-viewer-nav}]
     [pdf-viewer/view
      (merge {:source {:uri uri}
              :style  {:flex 1}}
             pdf-viewer-props)]]))
