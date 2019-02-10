#include "desktoplinking.h"
#include "bridge.h"
#include "eventdispatcher.h"

#include <QCoreApplication>
#include <QDebug>
#include <QDesktopServices>
#include <QUrl>
#include <QFileOpenEvent>

Q_LOGGING_CATEGORY(LINKING, "RCTLinking")

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<DesktopLinking *>(); }
} registerMetaType;
} // namespace

class DesktopLinkingPrivate {
public:
  Bridge *bridge = nullptr;
};

DesktopLinking::DesktopLinking(QObject *parent)
    : QObject(parent), d_ptr(new DesktopLinkingPrivate) {

  QCoreApplication::instance()->installEventFilter(this);
  connect(this, &DesktopLinking::urlOpened, this, &DesktopLinking::handleURL);
}

DesktopLinking::~DesktopLinking() {
}

void DesktopLinking::setBridge(Bridge *bridge) {
  Q_D(DesktopLinking);
  d->bridge = bridge;
}

QString DesktopLinking::moduleName() { return "DesktopLinking"; }

QList<ModuleMethod *> DesktopLinking::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap DesktopLinking::constantsToExport() { return QVariantMap(); }

void DesktopLinking::handleURL(const QString url) {
    Q_D(DesktopLinking);
    qCDebug(LINKING) << "::handleURL - path:" << url;
    d->bridge->eventDispatcher()->sendDeviceEvent("urlOpened", QVariantMap{{"url", url}});
}

bool DesktopLinking::eventFilter(QObject* obj, QEvent* event) {
    if (event->type() == QEvent::FileOpen)
    {
        QFileOpenEvent* fileEvent = static_cast<QFileOpenEvent*>(event);
        if (!fileEvent->url().isEmpty())
        {
            auto m_lastUrl = fileEvent->url().toString();
            emit urlOpened(m_lastUrl);
        }
        else if (!fileEvent->file().isEmpty())
        {
            emit fileOpened(fileEvent->file());
        }

        return false;
    }
    else
    {
        // standard event processing
        return QObject::eventFilter(obj, event);
    }
}