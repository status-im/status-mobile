(ns status-im2.contexts.shell.bottom-tabs
  (:require [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.style :as styles]
            [status-im2.contexts.shell.constants :as constants]
            [status-im2.contexts.shell.animation :as animation]
            [quo2.components.navigation.bottom-nav-tab :as bottom-nav-tab]))

(defn bottom-tab [icon stack-id shared-values]
  [bottom-nav-tab/bottom-nav-tab
   {:test-ID stack-id
    :icon                icon
    :icon-color-anim     (get
                          shared-values
                          (get constants/tabs-icon-color-keywords stack-id))
    :on-press            #(animation/bottom-tab-on-press stack-id)
    :accessibility-label (str (name stack-id) "-tab")}])

(defn bottom-tabs []
  [:f>
   (fn []
     (let [shared-values  @animation/shared-values-atom
           original-style (styles/bottom-tabs-container @animation/pass-through?)
           animated-style (reanimated/apply-animations-to-style
                           {:height (:bottom-tabs-height shared-values)}
                           original-style)]
       (animation/load-stack @animation/selected-stack-id)
       [reanimated/view {:style animated-style}
        [rn/view {:style (styles/bottom-tabs)}
         [bottom-tab :i/communities :communities-stack shared-values]
         [bottom-tab :i/messages :chats-stack shared-values]
         [bottom-tab :i/wallet :wallet-stack shared-values]
         [bottom-tab :i/browser :browser-stack  shared-values]]]))])
