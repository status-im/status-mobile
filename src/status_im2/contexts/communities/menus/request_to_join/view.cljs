(ns status-im2.contexts.communities.menus.request-to-join.view
  (:require [quo.react-native :as rn]
            [quo2.core :as quo]
            [quo.components.safe-area :as safe-area]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.communities.menus.request-to-join.style :as style]
            [utils.re-frame :as rf]
            [utils.requests :as requests]))

;; TODO: update with real data
(def community-rules
  [{:index   1
    :title   "Be respectful"
    :content
    "You must respect all users, regardless of your liking towards them. Treat others the way you want to be treated."}
   {:index   2
    :title   "No Inappropriate Language"
    :content
    "The use of profanity should be kept to a minimum. However, any derogatory language towards any user is prohibited."}
   {:index   3
    :title   "No spamming"
    :content
    "Don't send a lot of small messages right after each other. Do not disrupt chat by spamming."}
   {:index   4
    :title   "No pornographic, adult or NSFW material"
    :content "This is a community server and not meant to share this kind of material."}
   {:index   5
    :title   "No advertisements"
    :content
    "We do not tolerate any kind of advertisements, whether it be for other communities or streams."}
   {:index   6
    :title   "No offensive names and profile pictures"
    :content "You will be asked to change your name or picture if the staff deems them inappropriate."}])

(defn request-to-join-text
  [is-open?]
  (if is-open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join)))

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

(defn community-rules-list
  [rules]
  [rn/flat-list
   {:shows-horizontal-scroll-indicator false
    :data                              rules
    :separator                         [rn/view {:margin-top 1}]
    :render-fn                         community-rule-item}])

(defn request-to-join
  [{:keys [permissions name id images
           can-join? can-request-access?
           requested-to-join-at]}]
  (let [agreed-to-rules? (reagent/atom false)]
    [:f>
     (fn []
       (let [{window-height :height} (rn/use-window-dimensions)
             safe-area               (safe-area/use-safe-area)
             is-open?                (not= 3 (:access permissions))]
         [rn/scroll-view {:style {:max-height (- window-height (:top safe-area))}}
          [rn/view style/page-container
           [rn/view
            {:style style/title-container}

            [quo/text
             {:accessibility-label :communities-join-community
              :weight              :semi-bold
              :size                :heading-1}
             (request-to-join-text is-open?)]
            [rn/view
             {:style style/request-icon}
             [quo/icon :i/info]]]
           [quo/context-tag
            {:style
             {:margin-right :auto
              :margin-top   8}}
            (:thumbnail images) name]
           [quo/text
            {:style               {:margin-top 24}
             :accessibility-label :communities-rules-title
             :weight              :semi-bold
             :size                :paragraph-1}
            (i18n/label :t/community-rules)]

           [community-rules-list community-rules]

           [quo/disclaimer
            {:accessibility-label :rules-disclaimer-checkbox
             :container-style     {:margin-top 20}
             :on-change           #(swap! agreed-to-rules? not)}
            (i18n/label :t/accept-community-rules)]

           [rn/view {:style (style/bottom-container safe-area)}
            [quo/button
             {:accessibility-label :cancel
              :on-press            #(rf/dispatch [:bottom-sheet/hide])
              :type                :grey
              :style               style/cancel-button} (i18n/label :t/cancel)]

            [quo/button
             {:accessibility-label :join-community-button
              :on-press            (fn []
                                     (if can-join?
                                       (do
                                         (rf/dispatch [:communities/join id])
                                         (rf/dispatch [:bottom-sheet/hide]))
                                       (do (and can-request-access?
                                                (not (pos? requested-to-join-at))
                                                (requests/can-request-access-again?
                                                 requested-to-join-at))
                                           (rf/dispatch [:communities/request-to-join id])
                                           (rf/dispatch [:bottom-sheet/hide]))))
              :disabled            (not @agreed-to-rules?)
              :style               {:flex 1}} (request-to-join-text is-open?)]]]]))]))
