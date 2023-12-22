(ns status-im.contexts.preview-screens.quo-preview.text-combinations.username
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :name-type
    :type    :select
    :options [{:key :default}
              {:key   :ens
               :value "ENS"}
              {:key :nickname}]}
   {:key  :username
    :type :text}
   {:key  :name
    :type :text}
   {:key     :status
    :type    :select
    :options [{:key   nil
               :value "(Nothing)"}
              {:key :verified}
              {:key :contact}
              {:key :untrustworthy}
              {:key :untrustworthy-contact}
              {:key :blocked}]}
   {:key  :blur?
    :type :boolean}])

(defn- set-username-based-on-name-type
  [_ ratom {previous-type :name-type} {new-type :name-type}]
  (when (not= previous-type new-type)
    (swap! ratom assoc
      :username
      (if (= new-type :ens)
        "juan.eth"
        "Juanito Mdz"))))

(defn view
  []
  (let [state (reagent/atom {:name-type :default
                             :username  "Juanito Mdz"
                             :name      "Juan Méndez"
                             :status    nil
                             :blur?     true})
        _ (add-watch state :on-state-change set-username-based-on-name-type)]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? true
        :blur?                 (:blur? @state)}
       [quo/username @state]])))
