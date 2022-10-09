(ns status-im.ui.screens.communities.request-to-join-bottom-sheet-redesign
  (:require [status-im.i18n.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo2.components.markdown.text :as text]
            [quo2.components.buttons.button :as button]
            [quo2.components.selectors.disclaimer :as disclaimer]
            [quo2.components.icon :as icon]
            [status-im.utils.handlers :refer [>evt]]
            [quo2.components.tags.context-tags :as context-tags]
            [status-im.communities.core :as communities]
            [quo2.foundations.colors :as colors]))

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
       :background-color colors/white
       :border-color colors/neutral-20
       :border-width 1
       :border-radius 6}}
     [text/text {:style
                 {:margin-left :auto
                  :margin-right :auto
                  :margin-top :auto
                  :margin-bottom :auto}
                 :accessibility-label :communities-rule-index
                 :weight              :medium
                 :size                :label}

      (str index)]]
    [text/text
     {:accessibility-label :communities-rule-title
      :weight              :semi-bold
      :size                :paragraph-2}
     title]]
   [text/text
    {:style {:margin-left 28 :margin-top 1}
     :accessibility-label :communities-rule-content
     :size :paragraph-2}
    content]])

(defn community-rules-list [rules]
  [list/flat-list
   {:shows-horizontal-scroll-indicator false
    :data                              rules
    :separator [react/view {:margin-top 1}]
    :render-fn                         community-rule-item}])

(defn request-to-join [community]
  (let [agreed-to-rules? (reagent/atom false)]
    (fn []
      [react/view {:style {:flex 1 :margin-left 20 :margin-right 20 :margin-bottom 20}}
       [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :space-between}}

        [text/text {:accessibility-label :communities-join-community
                    :weight              :semi-bold
                    :size                :heading-1}
         (i18n/label :t/join-open-community)]
        [react/view {:style {:height 32
                             :width 32
                             :align-items :center
                             :background-color colors/white
                             :border-color colors/neutral-20
                             :border-width 1
                             :border-radius 8
                             :display :flex
                             :justify-content :center}}
         [icon/icon  :main-icons2/info]]]
       ;; TODO get tag image from community data 
       [context-tags/context-tag
        {:style
         {:margin-right :auto
          :margin-top 8}}
        (resources/get-image :status-logo) (:name community)]
       [text/text {:style {:margin-top 24}
                   :accessibility-label :communities-rules-title
                   :weight              :semi-bold
                   :size                :paragraph-1}
        (i18n/label :t/community-rules)]
       [community-rules-list community-rules]

       [disclaimer/disclaimer
        {:container-style {:margin-top 20}
         :on-change #(swap! agreed-to-rules? not)}
        (i18n/label :t/accept-community-rules)]

       [react/view {:style {:width "100%"
                            :margin-top 32 :margin-bottom 16
                            :flex 1
                            :flex-direction :row
                            :align-items :center
                            :justify-content :space-evenly}}
        [button/button {:on-press #(>evt [:bottom-sheet/hide])
                        :type :grey :style {:flex 1 :margin-right 12}}   (i18n/label :t/cancel)]
        [button/button
         {:on-press (fn []
                      (>evt [::communities/join (:id community)])
                      (>evt [:bottom-sheet/hide]))
          :disabled  (not @agreed-to-rules?) :style {:flex 1}}   (i18n/label :t/join-open-community)]]])))
