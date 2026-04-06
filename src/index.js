export default {
  async fetch(request, env) {
    const result = await env.DB
      .prepare("SELECT name FROM sqlite_master WHERE type='table'")
      .all();

    return Response.json(result);
  },
};