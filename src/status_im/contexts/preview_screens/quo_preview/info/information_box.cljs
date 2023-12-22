(ns status-im.contexts.preview-screens.quo-preview.info.information-box
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :informative}
              {:key :error}]}
   {:key  :closable?
    :type :boolean}
   {:key  :message
    :type :text}
   {:key  :button-label
    :type :text}])

(defn view
  []
  (let [state     (reagent/atom {:type         :default
                                 :closable?    true
                                 :message      (str "If you registered a stateofus.eth name "
                                                    "you might be eligible to connect $ENS")
                                 :button-label "Button"})
        closable? (reagent/cursor state [:closable?])
        closed?   (reagent/cursor state [:closed?])
        on-close  (fn []
                    (reset! closed? true)
                    (js/setTimeout (fn [] (reset! closed? false))
                                   2000))]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/information-box
        (merge {:icon     :i/info
                :style    {:width 335}
                :on-close (when @closable? on-close)}
               @state)
        (:message @state)]])))
