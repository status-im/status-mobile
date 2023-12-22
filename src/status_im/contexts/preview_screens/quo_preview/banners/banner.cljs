(ns status-im.contexts.preview-screens.quo-preview.banners.banner
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :latest-pin-text
    :type :text}
   {:key  :pins-count
    :type :text}
   {:key  :hide-pin?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:hide-pin? false
                             :pins-count 2
                             :latest-pin-text
                             "Be respectful of fellow community members, even if they"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/banner @state]])))
