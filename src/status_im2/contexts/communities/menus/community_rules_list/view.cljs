(ns status-im2.contexts.communities.menus.community-rules-list.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.communities.menus.community-rules-list.style :as style]
            [quo2.core :as quo]))

;; TODO: update with real data
(def rules
  [{:index 1
    :title "Be respectful"
    :content
    "You must respect all users, regardless of your liking towards them. Treat others the way you want to be treated."}
   {:index 2
    :title "No Inappropriate Language"
    :content
    "The use of profanity should be kept to a minimum. However, any derogatory language towards any user is prohibited."}
   {:index 3
    :title "No spamming"
    :content
    "Don't send a lot of small messages right after each other. Do not disrupt chat by spamming."}
   {:index   4
    :title   "No pornographic, adult or NSFW material"
    :content "This is a community server and not meant to share this kind of material."}
   {:index 5
    :title "No advertisements"
    :content
    "We do not tolerate any kind of advertisements, whether it be for other communities or streams."}
   {:index   6
    :title   "No offensive names and profile pictures"
    :content "You will be asked to change your name or picture if the staff deems them inappropriate."}])

(defn community-rule-item
  [{:keys [title content index]}]
  [rn/view
   {:style style/community-rule-container}
   [rn/view
    {:style style/inner-community-rule-container}
    [rn/view
     {:style style/community-rule}
     [quo/text
      {:style               style/community-rule-text
       :accessibility-label :communities-rule-index
       :weight              :medium
       :size                :label}
      (str index)]]
    [quo/text
     {:accessibility-label :communities-rule-title
      :weight              :semi-bold
      :size                :paragraph-2}
     title]]
   [quo/text
    {:style               style/community-rule-sub-text
     :accessibility-label :communities-rule-content
     :size                :paragraph-2}
    content]])

(defn view
  [rules]
  [rn/flat-list
   {:shows-horizontal-scroll-indicator false
    :data                              rules
    :separator                         [rn/view {:margin-top 1}]
    :render-fn                         community-rule-item}])
