(ns status-im2.navigation.screens
  (:require [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors] ;; TODO remove when not used anymore
            [react-native.platform :as platform]
            [status-im.ui.screens.screens :as old-screens]
            [status-im2.config :as config]
            [status-im2.contexts.activity-center.view :as activity-center]
            [status-im2.contexts.add-new-contact.views :as add-new-contact]
            [status-im2.contexts.chat.lightbox.view :as lightbox]
            [status-im2.contexts.chat.messages.view :as chat]
            [status-im2.contexts.chat.photo-selector.album-selector.view :as album-selector]
            [status-im2.contexts.chat.photo-selector.view :as photo-selector]
            [status-im2.contexts.communities.discover.view :as communities.discover]
            [status-im2.contexts.communities.overview.view :as communities.overview]
            [status-im2.contexts.onboarding.common.intro.view :as intro]
            [status-im2.contexts.onboarding.new-to-status.view :as new-to-status]
            [status-im2.contexts.onboarding.profiles.view :as profiles]
            [status-im2.contexts.quo-preview.main :as quo.preview]
            [status-im2.contexts.shell.view :as shell]
            [status-im2.contexts.syncing.view :as settings-syncing]))

(def components
  [])

(def transparent-screen-options
  (merge
   {:topBar                 {:visible false}
    :modalPresentationStyle :overCurrentContext
    :layout                 {:componentBackgroundColor :transparent
                             :orientation              :portrait
                             :backgroundColor          :transparent}}
   (if platform/android?
     {:navigationBar {:backgroundColor colors/neutral-100}
      :statusBar     {:backgroundColor :transparent
                      :style           :light
                      :drawBehind      true}}
     {:statusBar {:style :light}})))

(defn screens
  []
  (concat
   (old-screens/screens)
   [{:name      :intro
     :options   {:topBar {:visible false}}
     :insets    {:top false}
     :component intro/view}

    {:name      :activity-center
     :insets    {:top false}
     :options   transparent-screen-options
     :component activity-center/view}

    {:name      :shell-stack
     :insets    {:top false}
     :component shell/shell-stack}

    {:name      :chat
     :options   {:topBar {:visible false}}
     :component chat/chat}

    {:name      :lightbox
     :insets    {:top false :bottom false}
     :options   {:topBar        {:visible false}
                 :statusBar     {:backgroundColor colors/black
                                 :style           :light
                                 :animate         true}
                 :navigationBar {:backgroundColor colors/black}
                 :animations    {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                                    :toId          :shared-element
                                                                    :interpolation {:type   :decelerate
                                                                                    :factor 1.5}}]}
                                 :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                                    :toId          :shared-element
                                                                    :interpolation {:type
                                                                                    :decelerate
                                                                                    :factor 1.5}}]}}}
     :component lightbox/lightbox}
    {:name      :photo-selector
     :options   {:topBar {:visible false}}
     :component photo-selector/photo-selector}

    {:name      :album-selector
     :options   {:topBar                 {:visible false}
                 :modalPresentationStyle (if platform/ios? :overCurrentContext :none)}
     :component album-selector/album-selector}

    {:name      :new-contact
     :options   {:topBar {:visible false}}
     :component add-new-contact/new-contact}

    {:name      :discover-communities
     :options   {:topBar {:visible false}}
     :component communities.discover/discover}

    {:name      :community-overview
     :options   {:topBar {:visible false}}
     :component communities.overview/overview}

    {:name      :settings-syncing
     :insets    {:bottom true}
     :options   {:topBar {:title {:text (i18n/label :t/syncing)}}}
     :component settings-syncing/views}

    ;; Onboarding
    {:name      :profiles
     :insets    {:top false}
     :component profiles/views}

    ;; Onborading - new to Status
    {:name      :new-to-status
     :options   {:statusBar     {:style :light}
                 :topBar        {:visible false}
                 :navigationBar {:backgroundColor quo2.colors/black}}
     :insets    {:bottom false}
     :component new-to-status/new-to-status}]

   (when config/quo-preview-enabled?
     quo.preview/screens)

   (when config/quo-preview-enabled?
     quo.preview/main-screens)))
