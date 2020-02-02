function getHost() {
  const host = window.location.host;
  if (host === "localhost:8080") {
    return "localhost:8190";
  } else {
    return host;
  }
}

export default {
  HTTP_BASE: 'http://' + getHost() + '/MIRestAdaptor',
}
