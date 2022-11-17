(ns status-im2.contexts.communities.requests.actions.view
  (:require [i18n.i18n :as i18n]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [status-im2.contexts.communities.requests.actions.style :as style]

            ;;TODO remove when not needed anymore
            [status-im.react-native.resources :as resources]))

;; TODO: update with real data
(def community-rules [{:index   1
                       :title   "Be respectful"
                       :content "You must respect all users, regardless of your liking towards them. Treat others the way you want to be treated."}
                      {:index   2
                       :title   "No Inappropriate Language"
                       :content "The use of profanity should be kept to a minimum. However, any derogatory language towards any user is prohibited."}
                      {:index   3
                       :title   "No spamming"
                       :content "Don't send a lot of small messages right after each other. Do not disrupt chat by spamming."}
                      {:index   4
                       :title   "No pornographic, adult or NSFW material"
                       :content "This is a community server and not meant to share this kind of material."}
                      {:index   5
                       :title   "No advertisements"
                       :content "We do not tolerate any kind of advertisements, whether it be for other communities or streams."}
                      {:index   6
                       :title   "No offensive names and profile pictures"
                       :content "You will be asked to change your name or picture if the staff deems them inappropriate."}])

(defn community-rule-item [{:keys [title content index]}]
  [rn/view {:flex 1 :margin-top 16}
   [rn/view {:flex 1 :flex-direction :row :align-items :center}
    [rn/view style/community-rule
     [quo/text {:style               style/community-rule-text
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
    {:style               {:margin-left 28 :margin-top 1}
     :accessibility-label :communities-rule-content
     :size                :paragraph-2}
    content]])

(defn community-rules-list [rules]
  [rn/flat-list
   {:shows-horizontal-scroll-indicator false
    :data                              rules
    :separator                         [rn/view {:margin-top 1}]
    :render-fn                         community-rule-item}])

(defn actions [community]
  (let [agreed-to-rules? (reagent/atom false)]
    (fn []
      ;; TODO shouldn't this be a drawer from quo2 ?
      [rn/view style/request-container1
       [rn/view style/request-container2
        [quo/text {:accessibility-label :communities-join-community
                   :weight              :semi-bold
                   :size                :heading-1}
         (i18n/label :t/join-open-community)]
        [rn/view style/request-icon
         [quo/icon :i/info]]]
       ;; TODO get tag image from community data
       [quo/context-tag
        {:style
         {:margin-right :auto
          :margin-top   8}}
        (resources/get-image :status-logo) (:name community)]
       [quo/text {:style               {:margin-top 24}
                  :accessibility-label :communities-rules-title
                  :weight              :semi-bold
                  :size                :paragraph-1}
        (i18n/label :t/community-rules)]
       [community-rules-list community-rules]

       [quo/disclaimer
        {:container-style {:margin-top 20}
         :on-change       #(swap! agreed-to-rules? not)}
        (i18n/label :t/accept-community-rules)]

       [rn/view style/request-button
        [quo/button {:on-press #(rf/dispatch [:bottom-sheet/hide])
                     :type     :grey :style {:flex 1 :margin-right 12}}
         (i18n/label :t/cancel)]
        [quo/button
         {:on-press (fn []
                      (rf/dispatch [:communities/join (:id community)])
                      (rf/dispatch [:bottom-sheet/hide]))
          :disabled (not @agreed-to-rules?) :style {:flex 1}}
         (i18n/label :t/join-open-community)]]])))
