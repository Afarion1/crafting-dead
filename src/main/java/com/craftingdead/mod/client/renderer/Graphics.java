package com.craftingdead.mod.client.renderer;

import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.craftingdead.mod.CraftingDead;
import com.craftingdead.mod.util.PlayerResource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class Graphics {

	private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

	public static void drawRectangle(double x, double y, double width, double height, int color, float alpha,
			boolean shadow) {
		if (shadow)
			drawRectangle(x - 1, y - 1, width + 2, height + 2, color, alpha * 0.3F, false);
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.color(f, f1, f2, alpha);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) x, (double) y + height, 0.0D).endVertex();
		bufferbuilder.pos((double) x + width, (double) y + height, 0.0D).endVertex();
		bufferbuilder.pos((double) x + width, (double) y, 0.0D).endVertex();
		bufferbuilder.pos((double) x, (double) y, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static void drawGradientRectangle(double x, double y, double width, double height, int startColor,
			int endColor, float alpha) {
		float f = (float) (startColor >> 24 & 255) / 255.0F;
		float f1 = (float) (startColor >> 16 & 255) / 255.0F;
		float f2 = (float) (startColor >> 8 & 255) / 255.0F;
		float f3 = (float) (startColor & 255) / 255.0F;
		float f4 = (float) (endColor >> 24 & 255) / 255.0F;
		float f5 = (float) (endColor >> 16 & 255) / 255.0F;
		float f6 = (float) (endColor >> 8 & 255) / 255.0F;
		float f7 = (float) (endColor & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos((double) x, (double) y + height, 0.0D).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos((double) x + width, (double) y + height, 0.0D).color(f5, f6, f7, f4).endVertex();
		bufferbuilder.pos((double) x + width, (double) y, 0.0D).color(f5, f6, f7, f4).endVertex();
		bufferbuilder.pos((double) x, (double) y, 0.0D).color(f1, f2, f3, f).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawTexturedRectangle(double x, double y, double width, double height) {
		drawTexturedRectangle(x, y, width, height, 0.0D, 1.0D, 1.0D, 0.0D);
	}

	public static void drawTexturedRectangle(double x, double y, double textureX, double textureY, double width,
			double height) {
		drawTexturedRectangle(width / 2, height / 2, width, height, textureX, textureY + height, textureX + width,
				textureY);
	}

	public static void drawTexturedRectangle(double x, double y, double width, double height, double u, double v,
			double u2, double v2) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, y + height, 0.0D).tex(u, v).endVertex();
		bufferbuilder.pos(x + width, y + height, 0.0D).tex(u2, v).endVertex();
		bufferbuilder.pos(x + width, y, 0.0D).tex(u2, v2).endVertex();
		bufferbuilder.pos(x, y, 0.0D).tex(u, v2).endVertex();
		tessellator.draw();
	}

	public static void drawTextScaled(String text, double x, double y, int color, boolean dropShadow, double scale) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0);
		GL11.glScaled(scale, scale, scale);
		MINECRAFT.fontRenderer.drawString(text, 0, 0, color, dropShadow);
		GL11.glPopMatrix();
	}

	public static ResourceLocation getPlayerAvatar(UUID playerUUID) {
		ResourceLocation resourceLocation = new ResourceLocation(CraftingDead.MOD_ID,
				"textures/avatars/" + playerUUID + ".png");
		ITextureObject object = MINECRAFT.getTextureManager().getTexture(resourceLocation);
		if (object == null) {
			ThreadDownloadImageData imageData = new ThreadDownloadImageData(null,
					PlayerResource.AVATAR_URL.getUrl(playerUUID),
					new ResourceLocation(CraftingDead.MOD_ID, "textures/gui/avatar.png"), null);
			MINECRAFT.getTextureManager().loadTexture(resourceLocation, imageData);
		}
		return resourceLocation;
	}

	public static void drawItemStack(ItemStack stack, int x, int y) {
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		RenderItem itemRender = MINECRAFT.getRenderItem();
		itemRender.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null)
			font = MINECRAFT.fontRenderer;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		itemRender.renderItemOverlayIntoGUI(font, stack, x, y, null);
		itemRender.zLevel = 0.0F;
	}

	public static void drawCenteredString(FontRenderer fontRenderer, String string, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(string, (float) (x - fontRenderer.getStringWidth(string) / 2), (float) y,
				color);
	}

	public static void drawRightAlignedString(FontRenderer fontRenderer, String text, int x, int y, boolean dropShadow,
			int color) {
		fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text), y, color, dropShadow);
	}

	public static boolean isInBox(int x, int y, int width, int height, int checkX, int checkY) {
		return (checkX >= x) && (checkY >= y) && (checkX <= x + width) && (checkY <= y + height);
	}

	public static void renderModel(IBakedModel model, VertexFormat vertextFormat) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, vertextFormat);
		for (BakedQuad bakedquad : model.getQuads(null, null, 0)) {
			buffer.addVertexData(bakedquad.getVertexData());
		}
		tessellator.draw();
	}

	public static void renderModel(IBakedModel model, VertexFormat vertexFormat, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, vertexFormat);
		for (BakedQuad bakedquad : model.getQuads(null, null, 0)) {
			LightUtil.renderQuadColor(buffer, bakedquad, color);
		}
		tessellator.draw();
	}

	@Nullable
	public static EnumHandSide getHandSide(TransformType transformType) {
		switch (transformType) {
		case FIRST_PERSON_LEFT_HAND:
		case THIRD_PERSON_LEFT_HAND:
			return EnumHandSide.LEFT;
		case FIRST_PERSON_RIGHT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
			return EnumHandSide.RIGHT;
		default:
			return null;
		}
	}

	public static void bind(ResourceLocation resourceLocation) {
		MINECRAFT.getTextureManager().bindTexture(resourceLocation);
	}

}