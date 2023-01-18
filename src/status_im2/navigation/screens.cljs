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
            [status-im2.config :as config]))

(def components
  [])

(defn screens
  []
  (concat (old-screens/screens)
          [{:name      :activity-center
            :options   {:topBar {:visible false}}
            :component activity-center/view}

           {:name      :shell-stack
            :insets    {:top false}
            :component shell/shell-stack}

           {:name      :chat
            :options   {:topBar {:visible false}}
            :component chat/chat}

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
