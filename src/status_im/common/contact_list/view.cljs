(ns status-im.common.contact-list.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.contact-list.style :as style]))

(defn contacts-section-header
  [{:keys [title index]}]
  [rn/view (style/contacts-section-header (= index 0))
   [quo/divider-label title]])
