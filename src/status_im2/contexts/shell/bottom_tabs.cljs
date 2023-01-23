(ns status-im2.contexts.shell.bottom-tabs
  (:require [quo2.components.navigation.bottom-nav-tab :as bottom-nav-tab]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.style :as styles]))

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
           shared-values      @animation/shared-values-atom
           original-style     (styles/bottom-tabs-container @animation/pass-through?)
           animated-style     (reanimated/apply-animations-to-style
                               {:height (:bottom-tabs-height shared-values)}
                               original-style)]
       (animation/load-stack @animation/selected-stack-id)
       [reanimated/view {:style animated-style}
        [rn/view {:style (styles/bottom-tabs)}
         [bottom-tab :i/communities :communities-stack shared-values notifications-data]
         [bottom-tab :i/messages :chats-stack shared-values notifications-data]
         [bottom-tab :i/wallet :wallet-stack shared-values notifications-data]
         [bottom-tab :i/browser :browser-stack shared-values notifications-data]]]))])
