(ns status-im2.contexts.share.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.share.style :as style]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [status-im2.common.qr-code.qr :as qr]
            [clojure.string :as string]
            [status-im2.constants :as const]
            ;;TODO(siddarthkay) : move the components below over to status-im2 ns
            ;; issue -> https://github.com/status-im/status-mobile/issues/15549
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]))

(def ^:const profile-tab-id 0)
(def ^:const wallet-tab-id 1)

(defn copy-text-and-show-toast
  [{:keys [text-to-copy post-copy-message]}]
  (react/copy-to-clipboard text-to-copy)
  (rf/dispatch [:share/show-successfully-copied-toast post-copy-message]))

(defn header
  []
  [rn/view
   [quo/button
    {:icon                true
     :type                :blur-bg
     :size                32
     :accessibility-label :close-activity-center
     :override-theme      :dark
     :style               style/header-button
     :on-press            #(rf/dispatch [:hide-popover])}
    :i/close]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/share)]])

(defn profile-tab
  [window-width]
  (let [multiaccount              (rf/sub [:multiaccount])
        emoji-hash                (string/join (get multiaccount :emoji-hash))
        qr-size                   (- window-width 64)
        public-pk                 (get multiaccount :public-key)
        profile-qr-url            (str const/status-profile-base-url public-pk)
        port                      (rf/sub [:mediaserver/port])
        key-uid                   (get multiaccount :key-uid)
        emoji-hash-max-width      (* window-width 0.76)
        link-to-profile-max-width (* window-width 0.70)
       ]
    [:<>
     [rn/view {:style style/qr-code-container}
      [qr/user-profile-qr-code
       {:window-width window-width
        :key-uid      key-uid
        :qr-size      qr-size
        :public-key   public-pk
        :port         port}]
      [rn/view {:style (style/profile-address-container qr-size)}
       [rn/view {:style style/profile-address-column}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  style/profile-address-label}
         (i18n/label :t/link-to-profile)]
        [rn/touchable-highlight
         {:active-opacity   1
          :underlay-color   colors/neutral-80-opa-1-blur
          :background-color :transparent
          :on-press         #(copy-text-and-show-toast
                              {:text-to-copy      profile-qr-url
                               :post-copy-message (i18n/label :t/link-to-profile-copied)})
          :on-long-press    #(copy-text-and-show-toast
                              {:text-to-copy      profile-qr-url
                               :post-copy-message (i18n/label :t/link-to-profile-copied)})}
         [quo/text
          {:style           (style/profile-address-content link-to-profile-max-width)
           :size            :paragraph-1
           :weight          :medium
           :ellipsize-mode  :middle
           :number-of-lines 1}
          profile-qr-url]]]
       [rn/view {:style style/share-button-container}
        [quo/button
         {:icon                true
          :type                :blur-bg
          :size                32
          :accessibility-label :link-to-profile
          :override-theme      :dark
          :on-press            #(list-selection/open-share {:message profile-qr-url})}
         :i/share]]]]

     [rn/view {:style style/emoji-hash-container}
      [rn/view {:style style/profile-address-container}
       [rn/view {:style style/profile-address-column}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  style/emoji-hash-label}
         (i18n/label :t/emoji-hash)]
        [rn/touchable-highlight
         {:active-opacity   1
          :underlay-color   colors/neutral-80-opa-1-blur
          :background-color :transparent
          :on-press         #(copy-text-and-show-toast
                              {:text-to-copy      emoji-hash
                               :post-copy-message (i18n/label :t/emoji-hash-copied)})
          :on-long-press    #(copy-text-and-show-toast
                              {:text-to-copy      emoji-hash
                               :post-copy-message (i18n/label :t/emoji-hash-copied)})}
         [rn/text {:style (style/emoji-hash-content emoji-hash-max-width)} emoji-hash]]]]
      [rn/view {:style style/share-button-container}
       [quo/button
        {:icon                true
         :type                :blur-bg
         :size                32
         :accessibility-label :link-to-profile
         :override-theme      :dark
         :style               {:margin-right 12}
         :on-press            #(copy-text-and-show-toast
                                {:text-to-copy      emoji-hash
                                 :post-copy-message (i18n/label :t/emoji-hash-copied)})
         :on-long-press       #(copy-text-and-show-toast
                                {:text-to-copy      emoji-hash
                                 :post-copy-message (i18n/label :t/emoji-hash-copied)})}
        :i/copy]]]]))

(defn wallet-tab
  []
   [rn/text {:style {:color colors/white :text-align :center}} "not implemented"])

(defn view
  []
  (let [selected-tab (reagent/atom profile-tab-id)]
    (fn[]
      [safe-area/consumer
       (fn [{:keys [top bottom]}]
         (let [window-width (rf/sub [:dimensions/window-width])]
           [rn/view {:style (style/screen-container window-width top bottom)}
            [header]
            [rn/view {:style style/tabs-container}
             [quo/segmented-control
              {:size                28
               :scrollable?         true
               :blur?               true
               :override-theme      :dark
               :style               style/tabs
               :fade-end-percentage 0.79
               :scroll-on-press?    true
               :fade-end?           true
               :on-change           #(reset! selected-tab %)
               :default-active      @selected-tab
               :data                [{:id    profile-tab-id
                                      :label (i18n/label :t/profile)}
                                     {:id    wallet-tab-id
                                      :label (i18n/label :t/wallet)}]}]]
            (if (= @selected-tab profile-tab-id)
              [profile-tab window-width]
              [wallet-tab])]))]

      )
    ))
