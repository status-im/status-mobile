(ns status-im.contexts.preview.quo.selectors.react
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.preview.quo.preview :as preview]))

(defn gen-quantity
  [max-count _]
  (rand-int max-count))

(def memo-gen-quantity (memoize gen-quantity))

(def descriptor
  [{:key  :hide-new-reaction-button?
    :type :boolean}
   {:label   "Reactions"
    :key     :reaction-ids
    :type    :multi-select
    :options (for [[reaction-id reaction] constants/reactions]
               {:key   reaction-id
                :value (string/capitalize
                        (last
                         (string/split (name reaction) #"/")))})}
   {:label "Max rand. reactions (helper)"
    :key   :max-count
    :type  :number}
   {:key     :use-case
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :pinned
               :value "Pinned"}]}])

(defn preview-react
  []
  (let [[state set-state]
        (rn/use-state {:hide-new-reaction-button? true
                       :max-count                 1000
                       :reaction-ids              [1 2 3]
                       :use-case                  :default})

        [pressed-reactions set-pressed-reactions] (rn/use-state #{1})

        reactions (mapv (fn [reaction-id]
                          {:emoji-reaction-id reaction-id
                           :emoji-id          reaction-id
                           :emoji             (get constants/reactions reaction-id)
                           :quantity          (memo-gen-quantity (:max-count state) reaction-id)
                           :own               (contains? pressed-reactions reaction-id)})
                        (:reaction-ids state))

        on-press (fn [reaction]
                   (let [reaction-id (:emoji-id reaction)]
                     (if (contains? pressed-reactions reaction-id)
                       (set-pressed-reactions (disj pressed-reactions reaction-id))
                       (set-pressed-reactions (conj pressed-reactions
                                                    reaction-id)))))]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [rn/view
      {:padding-bottom     150
       :padding-vertical   60
       :padding-horizontal 20
       :border-radius      16
       :background-color   (when (= :pinned (:use-case state))
                             (colors/custom-color :blue 50 10))
       :align-items        :flex-start}
      [quo/react
       {:reactions                 reactions
        :hide-new-reaction-button? (:hide-new-reaction-button? state)
        :use-case                  (:use-case state)
        :on-press                  on-press
        :on-long-press             identity
        :on-press-new              identity}]]]))
