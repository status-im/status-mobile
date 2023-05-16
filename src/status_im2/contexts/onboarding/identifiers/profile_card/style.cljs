(ns status-im2.contexts.onboarding.identifiers.profile-card.style
  (:require [react-native.reanimated :as reanimated]
            [quo2.components.avatars.user-avatar.style :as avatar-style]))

(defn card-container
  [background-style]
  {:style [(reanimated/apply-animations-to-style
            {}
            {:padding-horizontal         12
             :padding-top                12
             :padding-bottom             12
             :flex                       1
             :border-radius              16})
           background-style]})

(defn opacity
  [opacity normal-style]
  {:style (reanimated/apply-animations-to-style 
           {:opacity opacity} normal-style)})

(defn identicon-ring
  [ring-style]
  {:style [(reanimated/apply-animations-to-style
            {}
            {:position :absolute 
             :top 0 
             :left 0})
           ring-style]})

(defn identicon-ring-image
  []
  (avatar-style/outer :medium))