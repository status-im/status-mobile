(ns status-im2.navigation.screens
<<<<<<< HEAD
  (:require [utils.i18n :as i18n] ;; TODO remove when not used anymore
            [status-im.ui.screens.screens :as old-screens]
            [status-im2.contexts.activity-center.view :as activity-center]
            [status-im2.contexts.chat.messages.view :as chat]
            [status-im2.contexts.add-new-contact.views :as add-new-contact]
            [status-im2.contexts.communities.discover.view :as communities.discover]
            [status-im2.contexts.communities.overview.view :as communities.overview]
            [status-im2.contexts.quo-preview.main :as quo.preview]
            [status-im2.contexts.shell.view :as shell]
            [status-im2.contexts.syncing.view :as settings-syncing]
            [status-im2.contexts.chat.images-horizontal.view :as images-horizontal]
            [status-im2.config :as config]
            [quo.design-system.colors :as colors]))
=======
  (:require
   [react-native.platform :as platform]
   [status-im2.contexts.chat.photo-selector.view :as photo-selector]
   [utils.i18n :as i18n] ;; TODO remove when not used anymore
   [status-im.ui.screens.screens :as old-screens]
   [status-im2.contexts.activity-center.view :as activity-center]
   [status-im2.contexts.chat.messages.view :as chat]
   [status-im2.contexts.communities.discover.view :as communities.discover]
   [status-im2.contexts.communities.overview.view :as communities.overview]
   [status-im2.contexts.quo-preview.main :as quo.preview]
   [status-im2.contexts.shell.view :as shell]
   [status-im2.contexts.syncing.view :as settings-syncing]
   [status-im2.contexts.chat.lightbox.view :as lightbox]
   [status-im2.config :as config]
   [quo.design-system.colors :as colors]
   [status-im2.contexts.chat.photo-selector.album-selector.view :as album-selector]))
>>>>>>> e9cf18df1... feat: photo & album selector screens

(def components
  [])

(defn screens
  []
  (concat
   (old-screens/screens)
   [{:name      :activity-center
     :options   {:topBar {:visible false}}
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
                 :statusBar     {:backgroundColor colors/black-persist
                                 :style           :light
                                 :animate         true}
                 :navigationBar {:backgroundColor colors/black-persist}
                 :animations    {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                                    :toId          :shared-element
                                                                    :interpolation {:type :decelerate}}]}
                                 :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                                    :toId          :shared-element
                                                                    :interpolation {:type
                                                                                    :decelerate}}]}}}
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
     :component settings-syncing/views}]

   (when config/quo-preview-enabled?
     quo.preview/screens)

   (when config/quo-preview-enabled?
     quo.preview/main-screens)))
