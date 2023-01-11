(ns status-im2.navigation.screens
  (:require [utils.i18n :as i18n] ;; TODO remove when not used anymore
            [status-im.ui.screens.screens :as old-screens]
            [status-im2.contexts.activity-center.view :as activity-center]
            [status-im2.contexts.chat.messages.view :as chat]
            [status-im2.contexts.communities.discover.view :as communities.discover]
            [status-im2.contexts.communities.overview.view :as communities.overview]
            [status-im2.contexts.quo-preview.main :as quo.preview]
            [status-im2.contexts.shell.view :as shell]
            [status-im2.contexts.syncing.view :as settings-syncing]
<<<<<<< HEAD
            [status-im2.config :as config]))
=======
            [status-im2.setup.config :as config]
<<<<<<< HEAD
            [quo.design-system.colors :as colors]
<<<<<<< HEAD
            [status-im2.contexts.chat.images-horizontal.view :as images-horizontal]
            [status-im2.navigation.state :as state]
            [utils.re-frame :as rf]))
>>>>>>> cc112eeb1... updates
=======
=======
>>>>>>> accd5cae8... lint
            [status-im2.contexts.chat.images-horizontal.view :as images-horizontal]))
>>>>>>> 419cc70dd... feat: images album

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

    {:name      :images-horizontal
     :insets    {:top false :bottom false}
     :options   {:topBar     {:visible false}
                 :animations {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                                 :toId          :shared-element
                                                                 :interpolation {:type :decelerate}}]}
                              :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                                 :toId          :shared-element
                                                                 :interpolation {:type
                                                                                 :decelerate}}]}}}
     :component images-horizontal/images-horizontal}

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
