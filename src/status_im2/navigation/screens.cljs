(ns status-im2.navigation.screens
  (:require [status-im2.setup.config :as config]
            [status-im2.contexts.communities.discover.view :as communities.discover]
            [status-im2.contexts.communities.overview.view :as communities.overview]
            [status-im2.contexts.shell.view :as shell]
            [status-im2.contexts.quo-preview.main :as quo.preview]

            ;; TODO remove when not used anymore
            [status-im.ui2.screens.chat.home :as chat.home]
            [status-im.ui2.screens.chat.view :as chat]
            [status-im.ui.screens.screens :as old-screens]))

(def components
  [])

(defn screens []
  (concat (old-screens/screens)

          [{:name      :shell-stack
            :insets    {:top false}
            :component shell/shell-stack}

           {:name      :home
            :component chat.home/home}

           {:name      :chat
            :options   {:topBar {:visible false}}
            :component chat/chat}

           {:name      :discover-communities
            :options   {:topBar {:visible false}}
            :component communities.discover/discover}

           {:name      :community-overview
            :options   {:topBar {:visible false}}
            :component communities.overview/overview}]

          (when config/quo-preview-enabled?
            quo.preview/screens)

          (when config/quo-preview-enabled?
            quo.preview/main-screens)))
