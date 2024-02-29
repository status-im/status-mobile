(ns status-im.common.contact-list.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.contact-list.style :as style]))

(defn contacts-section-header
  [{:keys [title index]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view (style/contacts-section-header (= index 0) theme)
     [quo/divider-label title]]))
