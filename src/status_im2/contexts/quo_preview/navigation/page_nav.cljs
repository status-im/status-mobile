(ns status-im2.contexts.quo-preview.navigation.page-nav
  (:require [quo2.components.navigation.page-nav :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def ^:private descriptor
  [{:label   "Page nav variation"
    :key     :selected-variation
    :type    :select
    :options [{:key   :text-only?
               :value "Text only"}
              {:key   :align-left?
               :value "Align Left"}
              {:key   :align-left-top-down-text?
               :value "Align left top down text?"}
              {:key   :align-left-with-icon?
               :value "Align Left with icon ?"}
              {:key   :one-icon-align-left?
               :value "One icon on the left ?"}
              {:key   :one-icon-align-right?
               :value "One icon on the right ?"}
              {:key   :two-icons?
               :value "Two icons ?"}
              {:key   :user-icon?
               :value "User icon ?"}
              {:key   :empty?
               :value "Empty ?"}]}
   {:label   "Number of right icons"
    :key     :number-of-right-icons
    :type    :select
    :options [{:key   1
               :value 1}
              {:key   2
               :value 2}
              {:key   3
               :value 3}]}])

(def ^:private selected-variation
  (reagent/atom {:selected-variation    :text-only?
                 :number-of-right-icons 1}))

(defn- cool-preview
  []
  (let
    [right-icon {:background-color (if (colors/dark?)
                                     colors/neutral-80
                                     colors/neutral-20)
                 :icon             :i/placeholder
                 :icon-color       nil}
     base-props
     {:horizontal-description? true
      :one-icon-align-left? true
      :align-mid? false
      :page-nav-color :transparent
      :page-nav-background-uri ""
      :mid-section
      {:type :text-with-description
       :icon :i/placeholder
       :main-text "Status"
       :left-icon :i/placeholder
       :right-icon :i/placeholder
       :description "SNT"
       :description-color "black"
       :description-icon :i/placeholder
       :description-user-icon
       "https://i.picsum.photos/id/810/200/300.jpg?hmac=HgwlXd-OaLOAqhGyCiZDUb_75EgUI4u0GtS7nfgxd8s"}
      :left-section
      {:icon                  :i/unlocked
       :icon-background-color (if (colors/dark?)
                                colors/neutral-80
                                colors/neutral-20)}}
     create-variation #(merge %1 %2 {:mid-section (merge (:mid-section %1) (:mid-section %2))})
     variations
     {:text-only?                base-props
      :align-left?               (create-variation base-props {:align-mid? true})
      :one-icon-align-left?      (create-variation base-props
                                                   {:one-icon-align-left? true
                                                    :mid-section          {:type
                                                                           :text-with-one-icon}})
      :one-icon-align-right?     (create-variation base-props
                                                   {:one-icon-align-left? false
                                                    :mid-section          {:type
                                                                           :text-with-one-icon}})
      :two-icons?                (create-variation base-props
                                                   {:mid-section {:type :text-with-two-icons}})
      :user-icon?                (create-variation base-props
                                                   {:align-mid?              true
                                                    :horizontal-description? false
                                                    :mid-section             {:type
                                                                              :text-with-one-icon}})
      :empty?                    (create-variation base-props
                                                   {:mid-section-main-text   ""
                                                    :mid-section-description ""})
      :align-left-with-icon?     (create-variation base-props
                                                   {:align-mid?  true
                                                    :mid-section {:type :text-with-one-icon}})
      :align-left-top-down-text? (create-variation base-props
                                                   {:align-mid?              true
                                                    :horizontal-description? false
                                                    :mid-section             {:type
                                                                              :text-with-description}})}
     state (reagent/atom
            (-> (get variations (:selected-variation @selected-variation))
                (assoc :right-section-buttons
                       (repeat (:number-of-right-icons @selected-variation) right-icon))))]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [rn/view {:flex 1}
        [preview/customizer selected-variation descriptor]]
       [rn/view
        {:padding-vertical 30
         :flex-direction   :row
         :justify-content  :center}
        [quo2/page-nav @state]]])))

(def ^:private trackable-cool-preview (reagent/track cool-preview selected-variation))

(defn preview-page-nav
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [@trackable-cool-preview]
     :key-fn                    str}]])
