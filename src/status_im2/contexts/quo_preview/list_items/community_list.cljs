(ns status-im2.contexts.quo-preview.list-items.community-list
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.community.data :as data]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor-type
  {:type    :select
   :key     :type
   :options [{:key :discover}
             {:key :engage}
             {:key :share}]})

(def descriptor-locked
  {:type :boolean :key :locked?})

(def descriptor-unread-count
  {:type :number :key :unread-count})

(def descriptor-title
  {:type :text :key :title})

(def descriptor-blur
  {:type :boolean :key :blur?})

(def descriptor-member-stats
  [{:type :number :key :members-count}
   {:type :number :key :active-count}])

(def descriptors-base
  [descriptor-type
   descriptor-title
   (preview/customization-color-option)])

(def descriptors-type-discover
  (conj descriptors-base
        {:type    :select
         :key     :info
         :options [{:key :token-gated}
                   {:key :default}]}
        {:label "Member stats?"
         :type  :boolean
         :key   :members?}))

(def descriptors-type-engage
  (conj descriptors-base
        {:type    :select
         :key     :info
         :options [{:key :notification}
                   {:key :mention}
                   {:key :muted}
                   {:key :token-gated}
                   {:key :navigation}
                   {:key :default}]}))

(def descriptors-type-share
  (conj descriptors-base {:type :text :key :subtitle}))

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

(defn view
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
        [preview/preview-container {:state state :descriptor (descriptors @state)}
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
                                          :active-count  (:active-count @state)})})]]))))
