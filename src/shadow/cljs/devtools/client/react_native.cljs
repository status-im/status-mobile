(ns shadow.cljs.devtools.client.react-native
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [goog.net.XhrIo :as xhr]
            [shadow.cljs.devtools.client.env :as env]
            [status-im.reloader :as reloader]))

(defonce repl-ns-ref (atom nil))

(defonce socket-ref (volatile! nil))

(defn ws-msg [msg]
  (if-let [s @socket-ref]
    (.send s (pr-str msg))
    (js/console.warn "WEBSOCKET NOT CONNECTED" (pr-str msg))))

(defn devtools-msg
  ([x]
   (js/console.log x))
  ([x y]
   (js/console.log x y)))

(defn script-eval [code]
  (js/goog.global.eval code))

(defn do-js-load [sources]
  (doseq [{:keys [resource-name js] :as src} sources]
    (devtools-msg "load JS" resource-name)
    (env/before-load-src src)
    (script-eval (str js "\n//# sourceURL=" resource-name))))

(defn do-js-reload [msg sources complete-fn]
  (env/do-js-reload
   (assoc msg
          :log-missing-fn
          (fn [fn-sym]
            (devtools-msg (str "can't find fn " fn-sym)))
          :log-call-async
          (fn [fn-sym]
            (devtools-msg (str "call async " fn-sym)))
          :log-call
          (fn [fn-sym]
            (devtools-msg (str "call " fn-sym))))
   #(do-js-load sources)
   complete-fn))

(defn load-sources [sources callback]
  (if (empty? sources)
    (callback [])
    (xhr/send
     (env/files-url)
     (fn [_]
       (this-as ^goog req
                (let [content
                      (-> req
                          (.getResponseText)
                          (reader/read-string))]
                  (callback content))))

     "POST"
     (pr-str {:client  :browser
              :sources (into [] (map :resource-id) sources)})
     #js {"content-type" "application/edn; charset=utf-8"})))

(defn noop [& _])

(defn handle-build-complete [{:keys [info reload-info] :as msg}]
  (let [{:keys [sources]}
        info

        warnings
        (->> (for [{:keys [resource-name warnings] :as src} sources
                   :when (not (:from-jar src))
                   warning warnings]
               (assoc warning :resource-name resource-name))
             (distinct)
             (into []))]
    (when (seq warnings)
      (reloader/build-failed))
    (when (and env/autoload
               (or (empty? warnings) env/ignore-warnings))
      (reloader/build-competed)
      (let [sources-to-get (env/filter-reload-sources info reload-info)]

        (when (seq sources-to-get)
          (load-sources sources-to-get #(do-js-reload msg % noop)))))))

(defn repl-error [e]
  (js/console.error "repl/invoke error" (.-message e) e)
  (env/repl-error e))

(defn repl-invoke [{:keys [id js]}]
  (let [result (env/repl-call #(js/eval js) repl-error)]
    (-> result
        (assoc :id id)
        (ws-msg))))

(defn repl-require [{:keys [id sources reload-namespaces]} done]
  (let [sources-to-load
        (->> sources
             (remove (fn [{:keys [provides] :as src}]
                       (and (env/src-is-loaded? src)
                            (not (some reload-namespaces provides)))))
             (into []))]

    (load-sources
     sources-to-load
     (fn [sources]
       (do-js-load sources)
       (ws-msg {:type :repl/require-complete :id id})
       (done)))))

(defn repl-init [{:keys [repl-state id]} done]
  (reset! repl-ns-ref (get-in repl-state [:current :ns]))
  (load-sources
   ;; maybe need to load some missing files to init REPL
   (->> (:repl-sources repl-state)
        (remove env/src-is-loaded?)
        (into []))
   (fn [sources]
     (do-js-load sources)
     (ws-msg {:type :repl/init-complete :id id})
     (devtools-msg "REPL init successful")
     (done))))

(defn repl-set-ns [{:keys [id ns]}]
  (reset! repl-ns-ref ns)
  (ws-msg {:type :repl/set-ns-complete :id id :ns ns}))

;; FIXME: core.async-ify this
(defn handle-message [{:keys [type] :as msg} done]
  ;; (js/console.log "ws-msg" (pr-str msg))
  (case type
    :repl/invoke
    (repl-invoke msg)

    :repl/require
    (repl-require msg done)

    :repl/set-ns
    (repl-set-ns msg)

    :repl/init
    (repl-init msg done)

    :repl/ping
    (ws-msg {:type :repl/pong :time-server (:time-server msg) :time-runtime (js/Date.now)})

    :build-complete
    (handle-build-complete msg)

    :build-failure
    (reloader/build-failed)

    :build-init
    nil

    :build-start
    (reloader/build-start)

    :pong
    nil

    :client/stale
    (devtools-msg "Stale Client! You are not using the latest compilation output!")

    :client/no-worker
    (devtools-msg (str "watch for build \"" env/build-id "\" not running"))

    ;; default
    :ignored)

  (when-not (contains? env/async-ops type)
    (done)))

(defn ws-connect []
  (let [ws-url
        (env/ws-url :react-native)

        socket
        (js/WebSocket. ws-url)]

    (vreset! socket-ref socket)
    (set! (.-onmessage socket)
          (fn [e]
            (env/process-ws-msg (. e -data) handle-message)))

    (set! (.-onopen socket)
          (fn [_]
            ;; :module-format :js already patches provide
            (when (= "goog" env/module-format)
              ;; patch away the already declared exception
              (set! (.-provide js/goog) js/goog.constructNamespace_))

            (env/set-print-fns! ws-msg)

            (devtools-msg "WebSocket connected!")))

    (set! (.-onclose socket)
          (fn [_]
            ;; not a big fan of reconnecting automatically since a disconnect
            ;; may signal a change of config, safer to just reload the page
            (devtools-msg "WebSocket disconnected!")
            (vreset! socket-ref nil)
            (env/reset-print-fns!)))

    (set! (.-onerror socket)
          (fn [e]
            (js/console.error (str "WebSocket connect failed:" (.-message e) "\n"
                                   "It was trying to connect to: " (subs ws-url 0 (str/index-of ws-url "/" 6)) "\n"))))))

(when ^boolean env/enabled
  ;; disconnect an already connected socket, happens if this file is reloaded
  ;; pretty much only for me while working on this file
  (when-let [s @socket-ref]
    (devtools-msg "connection reset!")
    (set! (.-onclose s) (fn [_]))
    (.close s)
    (vreset! socket-ref nil))

  (ws-connect))
