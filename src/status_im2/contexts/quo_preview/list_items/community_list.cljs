(ns status-im2.contexts.quo-preview.list-items.community-list
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.community.data :as data]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor-type
  {:label   "Type:"
   :key     :type
   :type    :select
   :options [{:key :discover :value "Discover"}
             {:key :engage :value "Engage"}
             {:key :share :value "Share"}]})

(def descriptor-locked
  {:label "Locked?" :key :locked? :type :boolean})

(def descriptor-unread-count
  {:label "Unread count:" :key :unread-count :type :number})

(def descriptor-title
  {:label "Title:" :key :title :type :text})

(def descriptor-blur
  {:label "Blur?" :key :blur? :type :boolean})

(def descriptor-member-stats
  [{:label "Total member count:"
    :key   :members-count
    :type  :number}
   {:label "Active member count:"
    :key   :active-count
    :type  :number}])

(def descriptors-base
  [descriptor-type
   descriptor-title
   (preview/customization-color-option)])

(def descriptors-type-discover
  (conj descriptors-base
        {:label   "Info:"
         :key     :info
         :type    :select
         :options [{:key :token-gated :value "Token gated"}
                   {:key :default :value "Default"}]}
        {:label "Member stats?"
         :key   :members?
         :type  :boolean}))

(def descriptors-type-engage
  (conj descriptors-base
        {:label   "Info:"
         :key     :info
         :type    :select
         :options [{:key :notification :value "Notification"}
                   {:key :mention :value "Mention"}
                   {:key :muted :value "Muted"}
                   {:key :token-gated :value "Token gated"}
                   {:key :navigation :value "Navigation"}
                   {:key :default :value "Default"}]}))

(def descriptors-type-share
  (conj descriptors-base {:label "Subtitle:" :key :subtitle :type :text}))

(defn descriptors
  [{:keys [members? info] :as state}]
  (let [descs (case (:type state)
                :discover (cond-> descriptors-type-discover
                            members?
                            (into descriptor-member-stats)

                            (= info :token-gated)
                            (into [descriptor-locked]))
                :engage   (cond-> descriptors-type-engage
                            (= info :token-gated)
                            (into [descriptor-locked])

                            (= info :mention)
                            (into [descriptor-unread-count]))
                :share    descriptors-type-share
                nil)]
    (if (quo.theme/dark?)
      (into [descriptor-blur] descs)
      descs)))

(defn cool-preview
  []
  (let [state (reagent/atom {:blur?               false
                             :customization-color :blue
                             :info                :token-gated
                             :type                :discover
                             :members?            false
                             :locked?             false
                             :title               "Status"
                             :subtitle            "Subtitle"
                             :members-count       629200
                             :active-count        112100
                             :unread-count        5})]
    (fn []
      (let [customization-color (colors/custom-color-by-theme (:customization-color @state) 50 60)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:margin-bottom 20}}
          [preview/customizer state (descriptors @state)]
          [rn/view {:style {:margin-vertical 30 :align-items :center}}
           [quo/community-list-item
            (merge @state
                   {:container-style     {:width 335}
                    :logo                (resources/get-mock-image :status-logo)
                    :tokens              (:tokens data/community)
                    :customization-color customization-color
                    :on-press            #(js/alert "List item pressed")
                    :on-long-press       #(js/alert "Long pressed item")
                    :on-press-info       #(js/alert "Info pressed")
                    :members             (when (:members? @state)
                                           {:members-count (:members-count @state)
                                            :active-count  (:active-count @state)})})]]]]))))

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
