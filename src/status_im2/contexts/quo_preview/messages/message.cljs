(ns status-im2.contexts.quo-preview.messages.message
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :header? :type :boolean}
   {:key :reacted? :type :boolean}
   {:key :delivered? :type :boolean}
   {:key :pinned-by :type :text}
   (preview/customization-color-option)])

(defn- gen-quantity
  [max-count _]
  (rand-int max-count))

(def ^:private memo-gen-quantity (memoize gen-quantity))

(defn- normalize-state
  [state]
  (merge @state
         {:context {:pinned-by (:pinned-by @state)
                    :reactions (when (:reacted? @state)
                                 (mapv (fn [reaction-id]
                                         {:emoji-reaction-id reaction-id
                                          :emoji-id          reaction-id
                                          :emoji             (get constants/reactions reaction-id)
                                          :quantity          (memo-gen-quantity 10 reaction-id)
                                          ;; :own               (contains? @pressed-reactions
                                          ;; reaction-id)
                                         })
                                       [1 2]))}
         })
)
(defn view
  []
  (let [state (reagent/atom {:avatar-props        {:full-name           "Alisher Yakupov"
                                                   :ring?               true
                                                   :status-indicator?   true
                                                   :online?             true
                                                   :profile-picture     (resources/get-mock-image
                                                                         :user-picture-male5)
                                                   :customization-color :blue}
                             :author-props        {:primary-name   "Alisher Yakupov"
                                                   :secondary-name "zQ3...9d4Gs0"
                                                   :time-str       "09:30"}
                             :customization-color :blue
                             :pinned-by           "Steve"
                            }
              )]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical   50
                                    :padding-horizontal 20}}
       [quo/message (normalize-state state)]])))
