(ns status-im2.contexts.quo-preview.selectors.react
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(defn- gen-quantity
  [max-count _]
  (rand-int max-count))

(def ^:private memo-gen-quantity (memoize gen-quantity))

(def ^:private descriptor
  [{:label "Add reaction"
    :key   :add-reaction?
    :type  :boolean}
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
   {:label "Pinned"
    :key   :pinned?
    :type  :boolean}
   (preview/customization-color-option
    {:label "Pinned BG (test)"})])

(defn preview-react
  []
  (let [state               (reagent/atom {:add-reaction?       true
                                           :max-count           1000
                                           :reaction-ids        [1 2 3]
                                           :customization-color :blue
                                           :pinned?             false})
        reaction-ids        (reagent/cursor state [:reaction-ids])
        add-reaction?       (reagent/cursor state [:add-reaction?])
        max-count           (reagent/cursor state [:max-count])
        pinned?             (reagent/cursor state [:pinned?])
        customization-color (reagent/cursor state [:customization-color])
        pressed-reactions   (reagent/atom #{1})
        reactions           (reagent/reaction
                             (mapv #(assoc {}
                                           :emoji-id %
                                           :quantity (memo-gen-quantity @max-count %)
                                           :own      (boolean (some (fn [v] (= v %))
                                                                    @pressed-reactions)))
                                   @reaction-ids))]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:padding-bottom     150
         :padding-vertical   60
         :padding-horizontal 20
         :border-radius      16
         :background-color   (when @pinned?
                               (colors/custom-color @customization-color 50 10))
         :align-items        :flex-start}
        [quo/react
         {:reactions     @reactions
          :add-reaction? @add-reaction?
          :on-press      (fn [reaction]
                           (let [reaction-id    (:emoji-id reaction)
                                 change-pressed (partial swap! pressed-reactions)]
                             (if (contains? @pressed-reactions reaction-id)
                               (change-pressed disj reaction-id)
                               (change-pressed conj reaction-id))))
          :on-long-press identity
          :on-press-new  identity}]]])))
