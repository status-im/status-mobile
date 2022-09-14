(ns status-im.ui.screens.communities.request-to-join-bottom-sheet-redesign
  (:require [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.icon :as quo2.icon]
            [status-im.utils.handlers :refer [>evt]]
            [quo2.components.tags.context-tags :as quo2.context-tags]
            [status-im.communities.core :as communities]
            [quo2.foundations.colors :as quo2.colors]))

;; TODO: update with real data
(def community-rules [{:index 1
                       :title "Be respectful"
                       :content "You must respect all users, regardless of your liking towards them. Treat others the way you want to be treated."}
                      {:index 2
                       :title "No Inappropriate Language"
                       :content "The use of profanity should be kept to a minimum. However, any derogatory language towards any user is prohibited."}
                      {:index 3
                       :title "No spamming"
                       :content "Don't send a lot of small messages right after each other. Do not disrupt chat by spamming."}
                      {:index 4
                       :title "No pornographic, adult or NSFW material"
                       :content "This is a community server and not meant to share this kind of material."}
                      {:index 5
                       :title "No advertisements"
                       :content "We do not tolerate any kind of advertisements, whether it be for other communities or streams."}
                      {:index 6
                       :title "No offensive names and profile pictures"
                       :content "You will be asked to change your name or picture if the staff deems them inappropriate."}])

(defn community-rule-item [{:keys [title content index]}]
  [react/view
   {:style {:flex 1 :margin-top 16}}
   [react/view
    {:style
     {:flex 1
      :flex-direction :row
      :align-items :center}}
    [react/view
     {:style
      {:height 18
       :width 18
       :margin-left 1
       :margin-right 9
       :background-color quo2.colors/white
       :border-color quo2.colors/neutral-20
       :border-width 1
       :border-radius 6}}
     [quo2.text/text {:style
                      {:margin-left :auto
                       :margin-right :auto
                       :margin-top :auto
                       :margin-bottom :auto}
                      :accessibility-label :communities-rule-index
                      :weight              :medium
                      :size                :label}

      (str index)]]
    [quo2.text/text
     {:accessibility-label :communities-rule-title
      :weight              :semi-bold
      :size                :paragraph-2}
     title]]
   [quo2.text/text
    {:style {:margin-left 28}
     :accessibility-label :communities-rule-content
     :size :paragraph-2}
    content]])

(defn community-rules-list [rules]

  [list/flat-list
   {:shows-horizontal-scroll-indicator false
    :data                              rules
    :render-fn                         community-rule-item}])

(defn rules-checkbox [agreed-to-rules?]
  [react/view {:style
               {:flex 1
                :flex-direction :row
                :margin-top 20
                :background-color quo2.colors/neutral-20
                :padding 12
                :border-radius 12
                :border-width 1
                :border-color quo2.colors/neutral-30}}
   [quo/checkbox {:value       @agreed-to-rules?
                  :on-change #(swap! agreed-to-rules? not)}]
   [quo2.text/text {:accessibility-label :communities-accept-rules
                    :size                :paragraph-2
                    :style {:margin-left 8}}
    (i18n/label :t/accept-community-rules)]])

(defn request-to-join [community]
  (let [agreed-to-rules? (reagent/atom false)]
    (fn []
      [react/view {:style {:flex 1  :margin-left 20 :margin-right 20 :margin-bottom 20}}
       [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :space-between}}

        [quo2.text/text {:accessibility-label :communities-join-community
                         :weight              :semi-bold
                         :size                :heading-1}
         (i18n/label :t/join-open-community)]
        [react/view {:style {:height 32
                             :width 32
                             :align-items :center
                             :background-color quo2.colors/white
                             :border-color quo2.colors/neutral-20
                             :border-width 1
                             :border-radius 6
                             :display :flex
                             :justify-content :center}}
         [quo2.icon/icon  :main-icons2/info]]]

       [quo2.context-tags/context-tag {:style {:margin-right :auto :margin-top 8}} (get-in community [:images :thumbnail :uri]) (:name community)]

       [quo2.text/text {:style {:margin-top 24}
                        :accessibility-label :communities-rules-title
                        :weight              :semi-bold
                        :size                :paragraph-1}
        (i18n/label :t/community-rules)]
       [community-rules-list community-rules]
       [rules-checkbox agreed-to-rules?]

       [react/view {:style {:width "100%"
                            :margin-top 32 :margin-bottom 16
                            :flex 1
                            :flex-direction :row
                            :align-items :center
                            :justify-content :space-evenly}}
        [quo2.button/button {:on-press #(>evt [:bottom-sheet/hide])
                             :type :grey :style {:flex 1 :margin-right 12}}   (i18n/label :t/cancel)]
        [quo2.button/button
         {:on-press #(>evt [::communities/join (:id community)])
          :disabled  (not agreed-to-rules?) :style {:flex 1}}   (i18n/label :t/join-open-community)]]])))
