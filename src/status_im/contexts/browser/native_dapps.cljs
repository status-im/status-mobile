(ns status-im.contexts.browser.native-dapps
  (:require
    [quo.context :as quo.context]
    [status-im.contexts.chat.home.view :as chat]
    [status-im.contexts.communities.home.view :as communities]
    [status-im.contexts.wallet.home.view :as wallet]))

(defn- stack
  [stack-id & children]
  (let [theme (quo.context/use-theme)]
    (into [quo.context/provider {:theme theme :screen-id stack-id}]
          children)))

(def views
  {"communities.status" [stack
                         :screen/communities-stack
                         [communities/view]]
   "messages.status"    [stack
                         :screen/chats-stack
                         [chat/view]]
   "wallet.status"      [stack :screen/wallet-stack
                         [wallet/view]]})


(def metadata
  {"communities.status" {:title      "Communities"
                         :origin-url "communities.status"
                         :icon       :i/communities}
   "messages.status"    {:title      "Messages"
                         :origin-url "messages.status"
                         :icon       :i/messages}
   "wallet.status"      {:title      "Wallet"
                         :origin-url "wallet.status"
                         :icon       :i/wallet}})
