(ns status-im2.contexts.shell.bottom-tabs
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.reanimated :as ra]
            [status-im2.contexts.shell.style :as style]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [quo2.components.navigation.bottom-nav-tab :as bottom-nav-tab]))

(defn blur-overlay-params
  [style]
  {:style         style
   :blur-amount   30
   :blur-radius   25
   :blur-type     :transparent
   :overlay-color :transparent})

(defn bottom-tab
  [icon stack-id shared-values notifications-data]
  [bottom-nav-tab/bottom-nav-tab
   (assoc (get notifications-data stack-id)
          :test-ID             stack-id
          :icon                icon
          :icon-color-anim     (get
                                shared-values
                                (get shell.constants/tabs-icon-color-keywords stack-id))
          :on-press            #(animation/bottom-tab-on-press stack-id)
          :accessibility-label (str (name stack-id) "-tab"))])

(defn bottom-tabs
  []
  [:f>
   (fn []
     (let [notifications-data (rf/sub [:shell/bottom-tabs-notifications-data])
           pass-through?      (rf/sub [:shell/shell-pass-through?])
           shared-values      @animation/shared-values-atom
           original-style     (style/bottom-tabs-container pass-through?)
           animated-style     (ra/apply-animations-to-style
                               {:height (:bottom-tabs-height shared-values)}
                               original-style)]
       (animation/load-stack @animation/selected-stack-id)
       (ra/set-val (:pass-through? shared-values) pass-through?)
       [ra/view {:style animated-style}
        (when pass-through?
          [blur/view (blur-overlay-params style/bottom-tabs-blur-overlay)])
        [rn/view {:style (style/bottom-tabs)}
         [bottom-tab :i/communities :communities-stack shared-values notifications-data]
         [bottom-tab :i/messages :chats-stack shared-values notifications-data]
         [bottom-tab :i/wallet :wallet-stack shared-values notifications-data]
         [bottom-tab :i/browser :browser-stack shared-values notifications-data]]]))])
