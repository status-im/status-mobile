(ns status-im.contexts.preview.quo.banners.alert-banner
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :text
    :type :text}
   {:key  :button-text
    :type :text}
   {:key  :action?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:action?         true
                             :text            "Alert text"
                             :button-text     "Button"
                             :on-button-press #(js/alert "pressed")})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/alert-banner @state]])))
