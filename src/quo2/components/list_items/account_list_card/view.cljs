(ns quo2.components.list-items.account-list-card.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))


(defn- internal-view
  [{:keys [theme]}]
  [rn/view [text/text "asdf"]])

(def view (quo.theme/with-theme internal-view))
