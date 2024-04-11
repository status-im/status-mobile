(ns status-im.common.contact-list.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.contact-list.style :as style]))

(defn contacts-section-footer
  []
  [rn/view style/contacts-section-footer])

(defn contacts-section-header
  [{:keys [title]}]
  (let [theme (quo.theme/use-theme-value)]
    [quo/divider-label {:container-style (style/contacts-section-header theme)}
     title]))
