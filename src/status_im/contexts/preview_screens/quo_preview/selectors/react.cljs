(ns status-im.contexts.preview-screens.quo-preview.selectors.react
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn- gen-quantity
  [max-count _]
  (rand-int max-count))

(def ^:private memo-gen-quantity (memoize gen-quantity))

(def ^:private descriptor
  [{:key  :add-reaction?
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
  (let [state             (reagent/atom {:add-reaction? true
                                         :max-count     1000
                                         :reaction-ids  [1 2 3]
                                         :use-case      :default})
        pressed-reactions (reagent/atom #{1})]

    (fn []
      (let [reactions (mapv (fn [reaction-id]
                              {:emoji-reaction-id reaction-id
                               :emoji-id          reaction-id
                               :emoji             (get constants/reactions reaction-id)
                               :quantity          (memo-gen-quantity (:max-count @state) reaction-id)
                               :own               (contains? @pressed-reactions reaction-id)})
                            (:reaction-ids @state))]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view
          {:padding-bottom     150
           :padding-vertical   60
           :padding-horizontal 20
           :border-radius      16
           :background-color   (when (= :pinned (:use-case @state))
                                 (colors/custom-color :blue 50 10))
           :align-items        :flex-start}
          [quo/react
           {:reactions     reactions
            :add-reaction? (:add-reaction? @state)
            :use-case      (:use-case @state)
            :on-press      (fn [reaction]
                             (let [reaction-id    (:emoji-id reaction)
                                   change-pressed (partial swap! pressed-reactions)]
                               (if (contains? @pressed-reactions reaction-id)
                                 (change-pressed disj reaction-id)
                                 (change-pressed conj reaction-id))))
            :on-long-press identity
            :on-press-new  identity}]]]))))
